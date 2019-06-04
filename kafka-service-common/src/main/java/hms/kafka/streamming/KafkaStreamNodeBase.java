package hms.kafka.streamming;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

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

import hms.common.GenericUtils;

public abstract class KafkaStreamNodeBase<TCon, TRep> implements PollChainning{
	protected KafkaConsumer<UUID, byte[]> consumer;
	protected KafkaProducer<UUID, byte[]> producer;


	protected int timeout = 5000;
	private boolean shutdownNode = false; 
	private int pendingHeartbeat = 0;

	protected abstract Logger getLogger();	
	protected abstract Class<? extends TCon> getTConsumeManifest();
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
	protected boolean isChained() {
		return false;
	}
	
	protected List<? extends PollChainning> getSubChains() {
		return null;
	}
	
	protected KafkaStreamNodeBase() {
		this.ensureTopics();
		this.createProducer();
		this.createConsummer();
	}

	public void setTimeout(int timeoutInMillisecons) {
		this.timeout = timeoutInMillisecons;
	}

	protected void ensureTopic(String topic) {
		if(topic.indexOf("{")<0 && topic.indexOf("}")<0) {
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
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		
		this.configProducer(props);
		this.producer = new KafkaProducer<>(props);
	}
	
	protected void configConsummer(Properties consumerProps ) {
		return;
	}
	
	protected String applyTemplateToRepForTopic(String topic, Object value) {
		return topic;
	}	
	
	private void processSingleRecord(ConsumerRecord<UUID, byte[]> record) {
		try {
			//this.getLogger().info("Consuming {} {}", this.getConsumeTopic(), record.key());					
			HMSMessage<TCon> request = KafkaMessageUtils.getHMSMessage(this.getTConsumeManifest(), record);											
			TRep res = this.processRequest(request);
			if(this.getForwardTopic()!=null) {
				if(this.getForwardBackTopic() == null) {
					this.reply(request, res);
				}else {
					this.forward(request, res);
				}
			}
		} catch (Exception e) {
			this.getLogger().error("Consumer error {} {}", this.getConsumeTopic(), e.getMessage());
		}
	}
	
	private CompletableFuture<Void> hookSubChains(CompletableFuture<Void> mychain) {
		if (this.getSubChains() != null && this.getSubChains().size() > 0)
			if (this.getSubChains().size() > 1) {
				List<CompletableFuture<Void>> all = new ArrayList<CompletableFuture<Void>>();
				for (PollChainning sub : this.getSubChains()) {
					all.add(sub.hookPolling(mychain));
				}
				return CompletableFuture.allOf(GenericUtils.toArray(all));
			} else {
				return this.getSubChains().get(0).hookPolling(mychain);
			}
		else {
			return mychain;
		}
	}
	
	public CompletableFuture<Void> hookPolling(CompletableFuture<Void> aheadtasks) {		
			CompletableFuture<Void> mytask = 
						aheadtasks.thenRunAsync(this.pollRequestFromConsummer, this.getExecutorService())
								.thenRunAsync(this.intervalCleanupRunnable, this.getExecutorService());
			return hookSubChains(mytask);
	}	
	
	
	CompletableFuture<Void> rootHook = null;
	private void hookPollingAsRoot() {
		if(!isChained() && this.pendingHeartbeat <= 1) {
			this.pendingHeartbeat += 1;
			CompletableFuture<Void> mytask = (rootHook != null ? this.hookPolling(rootHook) :
					CompletableFuture.runAsync(this.pollRequestFromConsummer, this.getExecutorService())
							.thenRunAsync(this.intervalCleanupRunnable, this.getExecutorService()));
			this.rootHook = mytask.thenRunAsync(this.decreasePendingHeartbeat, this.getExecutorService());
		}
	}
	
	private Runnable decreasePendingHeartbeat = ()->{
		this.pendingHeartbeat--;
	};
	
	private Runnable pollRequestFromConsummer = () -> {		
		if (!shutdownNode) {
			ConsumerRecords<UUID, byte[]> records = this.consumer.poll(Duration.ofMillis(10));
			for (ConsumerRecord<UUID, byte[]> record : records) {
				this.processSingleRecord(record);
			}
		}
	};	
	
	private Runnable intervalCleanupRunnable = ()->{
		this.intervalCleanup();
	};
	
	private Observer heartbeatObserver;
	
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
		
		if(!this.isChained()) {			
			this.heartbeatObserver = (Observable arg0, Object arg1) -> {
				if(!shutdownNode) {
					hookPollingAsRoot();
				}else{
					hms.common.ServiceWaiter.getInstance().removeHeartbeatObserver(this.heartbeatObserver);
				}
			};
			hms.common.ServiceWaiter.getInstance().addHeartbeatObserver(this.heartbeatObserver);
		}
		
		this.getLogger().info("Consumer {} ready", this.getConsumeTopic());						 
	}

	protected void reply(HMSMessage<TCon> request, TRep value) {
		HMSMessage<TRep> replymsg = request.forwardRequest();
		replymsg.setData(value);
		String replytop = applyTemplateToRepForTopic(request.getCurrentResponsePoint(this.getForwardTopic()), value);
		try {
			//this.getLogger().info("Replying to {}", replytop);
			ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(replymsg, replytop);
			this.producer.send(record, (meta, ex)->{
				if(ex != null) {
					this.getLogger().error("Reply error: {}", ex.getMessage());
				}
			});
		} catch (IOException e) {
			this.getLogger().error("Reply error {} {}", replytop, e.getMessage());
		}
	}
	
	protected void forward(HMSMessage<TCon> request, TRep value) {
		HMSMessage<TRep> forwardReq = request.forwardRequest();
		forwardReq.setData(value);
		String forwardtopic = applyTemplateToRepForTopic(this.getForwardTopic(), value);
		try {
			forwardReq.addReponsePoint(this.getForwardBackTopic(), request.getData());
			//this.getLogger().info("forwarding: {}", forwardtopic);			
			ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(forwardReq, forwardtopic);					
			this.producer.send(record, (meta, ex)->{
				if(ex != null) {
					this.getLogger().error("forward error: {}", ex.getMessage());
				}
			});
		} catch (IOException e) {
			this.getLogger().error("Forward request error {} {}", forwardtopic, e.getMessage());
		}
	}

	/**
	 * note: these 2 (processRequest & processRequest) will be called in thread safe manner, worked as single thread
	 * to enable multiple processing, need to create multiple processing nodes. 
	 * */
	protected abstract TRep processRequest(HMSMessage<TCon> record);	
	protected void intervalCleanup() {
		return;
	}
	
	public void shutDown() {
		this.getLogger().info("Shutting down");
		this.shutdownNode = true;
		while(pendingHeartbeat != 0) {
			try {
				Thread.sleep(50);
				this.getLogger().info("waiting for shutdown");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.producer.close();
		this.consumer.close();	
	}
}
