package hms.kafka.streamming;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
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

public abstract class KafkaStreamNodeBase<TReq, TRep> {	
	protected KafkaConsumer<String, byte[]> consumer;	
	protected KafkaProducer<String, byte[]> producer;

	protected String consumeTopic;	
	protected String groupid;
	protected String server;
	protected int timeout = 5000;
	private Logger logger;
	private Class<TReq> reqManifest;
	protected KafkaStreamNodeBase(Logger logger, Class<TReq> reqManifest, String server, String groupid, String topic) {
		this.logger = logger;
		this.server = server;
		this.groupid = groupid;
		this.consumeTopic = topic;
		this.reqManifest = reqManifest;
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
        
        NewTopic cTopic = new NewTopic(topic, 2, (short)1);    
        CreateTopicsResult createTopicsResult = adminClient.createTopics(Arrays.asList(cTopic));
        try {
			createTopicsResult.all().get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Create topic error",e);
		}
	}	
	
	protected void ensureTopics() {
        this.ensureTopic(this.consumeTopic);
	}	
	
	protected void createProducer() {		
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        this.producer = new KafkaProducer<>(props);
	}	
	
	protected void createConsummer() {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupid);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");        
        this.consumer = new KafkaConsumer<>(consumerProps);    
        this.consumer.subscribe(Pattern.compile(this.consumeTopic));
        
        CompletableFuture.runAsync(()->{
            while(true) {
            	ConsumerRecords<String, byte[]> records = this.consumer.poll(Duration.ofMillis(100));
				for (ConsumerRecord<String, byte[]> record : records) {					
					try {
						this.processRequest(KafkaMessageUtils.getHMSMessage(this.reqManifest, record));
					} catch (IOException e) {
						logger.error("Consumer error "+consumeTopic, e.getMessage());
					}
				}
            }
        });
	}		
	
	protected void reply(HMSMessage<TReq> request, TRep value) {
		HMSMessage<TRep> replymsg = request.forwardRequest();
		replymsg.setData(value);
		String replytop = request.getCurrentResponsePoint();
		try {
			ProducerRecord<String, byte[]> record = KafkaMessageUtils.getProcedureRecord(replymsg, replytop);
			this.producer.send(record).get();
		} catch (IOException | InterruptedException | ExecutionException e) {
			logger.error("Reply message error", e.getMessage());
		}		
	}
	
	protected abstract void processRequest(HMSMessage<TReq> record) ;
}
