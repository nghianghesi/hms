package hms.kafka.streamming;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;

import hms.common.ServiceWaiter.IServiceChecker;

public abstract class KafkaStreamNodeBase<TCon, TRep> {
	protected KafkaConsumer<UUID, byte[]> consumer;
	protected KafkaProducer<UUID, byte[]> producer;


	protected int timeout = 5000;
	private boolean shutdownNode = false; 

	protected abstract Logger getLogger();	
	protected abstract Class<TCon> getTConsumeManifest();
	protected abstract String getConsumeTopic();
	protected abstract String getForwardTopic();
	
	/***
	 * set the point to handler response of the forward-message.
	 */	
	protected String getForwardBackTopic() {
		return null;
	}
	
	protected abstract String getGroupid();
	protected abstract String getServer();
	
	protected abstract Executor getExecutorService();
	
	protected KafkaStreamNodeBase() {
		this.ensureTopics();
		this.createProducer();
		this.createConsummer();
	}

	public void setTimeout(int timeoutInMillisecons) {
		this.timeout = timeoutInMillisecons;
	}

	protected void ensureTopic(String topic) {
		if(!topic.matches("{.*?}")) {
			Properties props = new Properties();
			props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.getServer());
			AdminClient adminClient = AdminClient.create(props);
	
			// by default, create topic with 1 partition, use Kafka tools to change this topic to scale.
			NewTopic cTopic = new NewTopic(topic, 1, (short) 1);
			CreateTopicsResult createTopicsResult = adminClient.createTopics(Arrays.asList(cTopic));
			try {
				createTopicsResult.all().get();
			} catch (InterruptedException | ExecutionException e) {
				this.getLogger().error("Create topic error {}", e.getMessage());
			}
		}
	}

	protected void ensureTopics() {
		this.ensureTopic(this.getConsumeTopic());
		if(this.getForwardTopic()!=null) {
			this.ensureTopic(this.getForwardTopic());
		}		
		
		if(this.getForwardBackTopic()!=null) {
			this.ensureTopic(this.getForwardBackTopic());
		}
	}
	protected void configProducer(Properties producerProps ) {
		return;
	}
	protected void createProducer() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.getServer());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.UUIDSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.ByteArraySerializer");
		this.configProducer(props);
		this.producer = new KafkaProducer<>(props);
	}
	
	protected void configConsummer(Properties consumerProps ) {
		return;
	}
	
	protected <T> String applyTemplateToRepForTopic(String topic, T res) {
		return topic;
	}
	
	private IServiceChecker<Void> consummerWaiter = new IServiceChecker<Void>(){
		public boolean isReady() {
			return true;
		}
		public Void getResult() {
			return null;
		}
		public boolean isError() {
			return false;
		}
		public Throwable getError() {		
			return null;
		}
	};
	
	
	private void processSingleRecord(ConsumerRecord<UUID, byte[]> record) {
		try {
			this.getLogger().info("Consuming {} {}", this.getConsumeTopic(), record.key());					
			HMSMessage<TCon> request = KafkaMessageUtils.getHMSMessage(this.getTConsumeManifest(), record);											
			TRep res = this.processRequest(request);
			if(this.getForwardTopic()!=null) {
				if(this.getForwardBackTopic() == null) {
					this.reply(request, res);
				}else {
					this.forward(request, res);
				}
			}
		} catch (IOException e) {
			this.getLogger().error("Consumer error {} {}", this.getConsumeTopic(), e.getMessage());
		}
	}
	
	private Runnable pollRequestFromConsummer;	
	protected void createConsummer() {
		Properties consumerProps = new Properties();
		consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.getServer());
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, this.getGroupid());
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.UUIDDeserializer");		
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.ByteArrayDeserializer");
		this.configConsummer(consumerProps);
		this.consumer = new KafkaConsumer<>(consumerProps);
		this.consumer.subscribe(Collections.singletonList(this.getConsumeTopic()));
		
		this.pollRequestFromConsummer = () -> {		
			if (!shutdownNode) {
				ConsumerRecords<UUID, byte[]> records = this.consumer.poll(Duration.ofMillis(1));
				for (ConsumerRecord<UUID, byte[]> record : records) {
					this.processSingleRecord(record);
				}
				
				if (!shutdownNode) { 
					hms.common.ServiceWaiter.getInstance()
						.waitForSignal(consummerWaiter, Integer.MAX_VALUE) // by ServiceWaiter --> will idle 100ms each round
					.thenRunAsync(this.pollRequestFromConsummer, this.getExecutorService());
				}else {
					this.notify();
				}
			}
		};
		CompletableFuture.runAsync(this.pollRequestFromConsummer, this.getExecutorService());
		
		this.getLogger().info("Consumer {} ready", this.getConsumeTopic());						 
	}

	protected void reply(HMSMessage<TCon> request, TRep value) {
		HMSMessage<TRep> replymsg = request.forwardRequest();
		replymsg.setData(value);
		String replytop = applyTemplateToRepForTopic(request.getCurrentResponsePoint(this.getForwardTopic()), value);
		try {
			ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(replymsg, replytop);
			this.producer.send(record).get();
		} catch (IOException | InterruptedException | ExecutionException e) {
			this.getLogger().error("Reply message error {}", e.getMessage());
		}
	}
	
	protected void forward(HMSMessage<TCon> request, TRep value) {
		HMSMessage<TRep> forwardReq = request.forwardRequest();
		forwardReq.setData(value);
		try {
			forwardReq.addReponsePoint(this.getForwardBackTopic(), request.getData());
			//this.getLogger().info("forwarding:" + forwardReq.DebugInfo());			
			String forwardtopic = applyTemplateToRepForTopic(this.getForwardTopic(), value);
			ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(forwardReq, forwardtopic);					
			this.producer.send(record).get();
		} catch (IOException | InterruptedException | ExecutionException e) {
			this.getLogger().error("Forward request error {}", e.getMessage());
		}		
	}

	protected abstract TRep processRequest(HMSMessage<TCon> record);
	
	public void shutDown() {
		this.getLogger().info("Shutting down");
		this.producer.close();
		this.consumer.close();	
		this.shutdownNode = true;
	}
}
