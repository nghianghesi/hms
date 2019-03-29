package hms.kafka.streamming;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

public abstract class KafkaStreamNodeBase<TReq, TRep> {
	protected KafkaConsumer<String, byte[]> consumer;
	protected KafkaProducer<String, byte[]> producer;

	protected String consumeTopic;
	protected String groupid;
	protected String server;
	protected int timeout = 5000;
	protected int numberOfExecutors = 5;
	private boolean shutdownNode = false; 
	protected Logger logger;
	private Class<TReq> reqManifest;

	protected KafkaStreamNodeBase(Logger logger, Class<TReq> reqManifest, String server, String groupid, String topic) {
		this(logger, reqManifest, server, groupid, topic, 5);
	}
	protected KafkaStreamNodeBase(Logger logger, Class<TReq> reqManifest, String server, String groupid, String topic, int numberOfExecutors) {
		this.logger = logger;
		this.server = server;
		this.groupid = groupid;
		this.consumeTopic = topic;
		this.reqManifest = reqManifest;
		this.numberOfExecutors = numberOfExecutors;
		this.ensureTopics();
		this.createProducer();
		this.createConsummer();
	}

	public void setTimeout(int timeoutInMillisecons) {
		this.timeout = timeoutInMillisecons;
	}

	protected void ensureTopic(String topic) {
		Properties props = new Properties();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
		AdminClient adminClient = AdminClient.create(props);

		NewTopic cTopic = new NewTopic(topic, 2, (short) 1);
		CreateTopicsResult createTopicsResult = adminClient.createTopics(Arrays.asList(cTopic));
		try {
			createTopicsResult.all().get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Create topic error {}", e.getMessage());
		}
	}

	protected void ensureTopics() {
		this.ensureTopic(this.consumeTopic);
	}

	protected void createProducer() {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.ByteArraySerializer");
		this.producer = new KafkaProducer<>(props);
	}
	
	

	protected void createConsummer() {
		Properties consumerProps = new Properties();
		consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupid);
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringDeserializer");		
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.ByteArrayDeserializer");
		this.consumer = new KafkaConsumer<>(consumerProps);
		this.consumer.subscribe(Collections.singletonList(this.consumeTopic));

		
		{// process record
			java.util.function.Consumer<ConsumerRecord<String, byte[]>> processRecord = (record)->{
				try {
					 HMSMessage<TReq> request = KafkaMessageUtils.getHMSMessage(this.reqManifest, record);
					logger.info("Consuming {} {}",consumeTopic, request.getRequestId());						 
					this.processRequest(request);
				} catch (IOException e) {
					logger.error("Consumer error {} {}", consumeTopic, e.getMessage());
				}
			};
	
			final ExecutorService ex = this.numberOfExecutors > 1 ? Executors.newFixedThreadPool(numberOfExecutors):null;
			final Semaphore executorSemaphore = this.numberOfExecutors > 1 ? new Semaphore(numberOfExecutors) : null;		
			java.util.function.Consumer<ConsumerRecord<String, byte[]>> processRecordByPool = 
			this.numberOfExecutors > 1 ? (record) -> {
				try {
					executorSemaphore.acquire();
				} catch (InterruptedException e) {
					logger.error("Consumer error {} {}", consumeTopic, e.getMessage());
				}
				ex.execute(()->{
					processRecord.accept(record);
					executorSemaphore.release();
				});			
			} : processRecord;
			
			Runnable cleanupPool = this.numberOfExecutors > 1 ? () -> {
				ex.shutdown();
				try {
					ex.awaitTermination(10, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					logger.error("Shutdown consumer error {} {}", consumeTopic, e.getMessage());
				}
			}:()->{};
			
			new Thread(() -> {		
				while (!shutdownNode) {
					ConsumerRecords<String, byte[]> records = this.consumer.poll(Duration.ofMillis(500));
					for (ConsumerRecord<String, byte[]> record : records) {
						processRecordByPool.accept(record);
					}
				}
				cleanupPool.run();
			}).start();
		}
		
		logger.info("Consumer {} ready {}",consumeTopic, this.numberOfExecutors);						 
	}

	protected void reply(HMSMessage<TReq> request, TRep value) {
		HMSMessage<TRep> replymsg = request.forwardRequest();
		replymsg.setData(value);
		String replytop = request.getCurrentResponsePoint();
		try {
			ProducerRecord<String, byte[]> record = KafkaMessageUtils.getProcedureRecord(replymsg, replytop);
			this.producer.send(record).get();
		} catch (IOException | InterruptedException | ExecutionException e) {
			logger.error("Reply message error {}", e.getMessage());
		}
	}

	protected abstract void processRequest(HMSMessage<TReq> record);
}
