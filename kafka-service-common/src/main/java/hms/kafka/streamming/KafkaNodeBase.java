package hms.kafka.streamming;

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
import org.slf4j.Logger;
import com.typesafe.config.Config;

public abstract class KafkaNodeBase {	
	protected KafkaProducer<String, byte[]> producer;
	protected KafkaConsumer<String, byte[]> consumer;
	protected String requestTopic;
	protected String consumeTopic;
	protected String groupid;
	protected String server;
	protected int timeout = 5000;
	private Logger logger;
	protected KafkaNodeBase(Logger logger, Config config) {
		this.logger = logger;
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
        
        NewTopic requestTopic = new NewTopic(this.requestTopic, 2, (short)1);
        NewTopic returnTopic = new NewTopic(this.consumeTopic, 2, (short)1);
        CreateTopicsResult createTopicsResult = adminClient.createTopics(Arrays.asList(requestTopic,returnTopic));
        try {
			createTopicsResult.all().get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Create topic error",e);
		}
	}	
	
	protected void createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        this.producer = new KafkaProducer<>(props);
	}
	
	protected void createConsummer() {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupid);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArrayDeserializer");        
        this.consumer = new KafkaConsumer<>(consumerProps);    
        this.consumer.subscribe(Pattern.compile(String.format("^%s.*", this.consumeTopic)));
        
        CompletableFuture.runAsync(()->{
            while(true) {
            	ConsumerRecords<String, byte[]> records = this.consumer.poll(Duration.ofMillis(100));
				for (ConsumerRecord<String, byte[]> record : records) {					
					this.processRequest(record);
				}
            }
        });
	}		
	
	protected abstract void processRequest(ConsumerRecord<String, byte[]> record) ;
	
	protected abstract void loadConfig(Config config);
}
