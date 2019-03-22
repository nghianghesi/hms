package hms.kafka.streamming;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;

public class KafkaProducerBase {
	protected KafkaProducer<String, byte[]> producer;
	
	protected String requestTopic;
	protected String server;
	protected int timeout = 5000;
	private Logger logger;
	
	protected KafkaProducerBase(Logger logger, String server, String topic) {
		this.logger = logger;
		this.server = server;
		this.requestTopic = topic;
		this.ensureTopic();
		this.createProducer();
	}	
	
	protected void createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        this.producer = new KafkaProducer<>(props);
	}
	
	protected void ensureTopic() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.server);
        AdminClient adminClient = AdminClient.create(props);
        
        NewTopic returnTopic = new NewTopic(this.requestTopic, 2, (short)1);
        CreateTopicsResult createTopicsResult = adminClient.createTopics(Arrays.asList(returnTopic));
        try {
			createTopicsResult.all().get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Create topic error",e);
		}
	}		
	
	public void setTimeout(int timeoutInMillisecons) {
		this.timeout = timeoutInMillisecons;
	}
	
}
