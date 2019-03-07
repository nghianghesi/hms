package hms.kafka;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

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

import com.typesafe.config.Config;

import hms.common.messaging.MessageBasedServiceManager;

public abstract class KafkaProducerBase {
	
	protected KafkaProducer<String, byte[]> producer;
	protected KafkaConsumer<String, byte[]> consumer;
	protected String requestTopic = "hms.provider";
	protected String returnTopic = "return.hms.provider";
	protected String groupid = "hms.provider.responselistener";
	protected String server;
	protected int timeout = 5000;
	protected MessageBasedServiceManager messageManager;
	private Logger logger;
	protected KafkaProducerBase(Logger logger, Config config, MessageBasedServiceManager messageManager) {
		this.logger = logger;
		this.messageManager = messageManager;
		this.server = config.getString("kafka.server");
		

		this.loadConfig(config);
		this.ensureTopic();
		this.createConsummer();
		this.createProducer();		
	}
	
	protected void ensureTopic() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        AdminClient adminClient = AdminClient.create(props);
        
        NewTopic topic = new NewTopic(this.requestTopic, 2, (short)1);
        CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(topic));
        try {
			createTopicsResult.all().get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Create topic error",e);
		}
	}
	
	protected byte[] toRequestBody(Object data) {
		try {
			return this.messageManager.toRequestBody(data);
		} catch (IOException e) {
			logger.error("Building request error");
			return null;
		}
	}
	
	protected <K,V> void setCommonInfo(ProducerRecord<K, V> record, long id) {
		record.headers().add(this.messageManager.REQUEST_ID_KEY,this.messageManager.longToBytes(id));
		record.headers().add(this.messageManager.RETURN_TOPIC_KEY,this.returnTopic.getBytes());		
	}
	
	protected <K,V> long getRecordRequestId(ConsumerRecord<K, V> record) {		
		return this.messageManager.bytesToLong(record.headers().lastHeader(this.messageManager.REQUEST_ID_KEY).value());
	}		
	
	protected void createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new KafkaProducer<>(props);
	}
	
	protected void createConsummer() {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupid);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");        
        this.consumer = new KafkaConsumer<>(consumerProps);    
        this.consumer.subscribe(Pattern.compile(String.format("^%s\\..*", this.returnTopic)));
        
        CompletableFuture.runAsync(()->{
            while(true) {
            	ConsumerRecords<String, byte[]> records = this.consumer.poll(Duration.ofMillis(100));
				for (ConsumerRecord<String, byte[]> record : records) {
					long requestid = this.getRecordRequestId(record);
					this.processResponse(requestid, record.value());
				}
            }
        });
	}		
	
	protected void processResponse(long requestid, byte[] data) {
		this.messageManager.handlerResponse(requestid, data);
	}
	
	protected abstract void loadConfig(Config config);
}
