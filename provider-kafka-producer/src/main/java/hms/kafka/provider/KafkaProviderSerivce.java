package hms.kafka.provider;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import com.typesafe.config.Config;

import hms.common.messaging.MessageBasedReponse;
import hms.common.messaging.MessageBasedRequest;
import hms.common.messaging.MessageBasedServiceManager;
import hms.dto.Provider;
import hms.dto.ProviderTracking;
import hms.provider.ProviderService;
import play.libs.Json;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

public class KafkaProviderSerivce implements hms.provider.IProviderService{
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderSerivce.class);
	
	private KafkaProducer<String, String> producer;
	private KafkaConsumer<String, String> consumer;
	private String topicName = "hms.provider";
	private String groupid = "hms.provider.responselistener";
	private String server;
	private int timeout = 5000;
	
	private MessageBasedServiceManager messageManager;
	@Inject
	public KafkaProviderSerivce(Config config, MessageBasedServiceManager messageManager) {
		if(config.hasPath("kafka.provider.topic")) {
			this.topicName = config.getString("kafka.provider.topic");
		}		
		
		if(config.hasPath("kafka.provider.timeout")) {
			this.timeout = (short)config.getInt("kafka.provider.timeout");
		}
		
		this.server = config.getString("kafka.server");
		this.createTopic();
		this.createConsummer();
		this.createProducer();
		this.messageManager = messageManager;
	}
	
	private void createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new KafkaProducer<String, String>(props);
	}
	
	private void createConsummer() {
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, this.groupid);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");        
        this.consumer = new KafkaConsumer < > (consumerProps);        
	}	
	
	private void createTopic() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        AdminClient adminClient = AdminClient.create(props);
        
        NewTopic topic = new NewTopic(this.topicName, 2, (short)1);
        CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(topic));
        try {
			createTopicsResult.all().get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Create topic error",e);
		}
	}
	
	@Override
	public CompletableFuture<Boolean> clear() {
		return CompletableFuture.supplyAsync(()->{
			MessageBasedReponse response = this.messageManager.request((request)->{				
				String messageBody = Json.toJson(request).toString();
				return this.producer.send(new ProducerRecord<String,String>(this.topicName, messageBody));
			}, this.timeout);			
			return !response.isError();
		});
	}

	@Override
	public CompletableFuture<Boolean> initprovider(Provider providerdto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> tracking(ProviderTracking trackingdto) {
		// TODO Auto-generated method stub
		return null;
	}

}
