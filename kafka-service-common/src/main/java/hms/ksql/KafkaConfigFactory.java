package hms.ksql;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

public class KafkaConfigFactory {
	public static <TK,TV> Properties getConsumerConfigs(Class<TK> keymanifest, Class<TV> datamanifest, String server, String groupid) {
		Properties consumerProps = new Properties();
		consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupid);
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, new DslJsonDeserializer<>(keymanifest));		
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, new DslJsonDeserializer<>(datamanifest));
		return consumerProps;
	}
	
	public static <TK,TV> Properties getProduceConfigs(String server) {
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, new DslJsonSerializer<TK>());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, new DslJsonSerializer<TV>());
		return  props;
	}
	
	public static void ensureTopic(String server, String topic) throws InterruptedException, ExecutionException {
		Properties props = new Properties();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, server);
		AdminClient adminClient = AdminClient.create(props);

		NewTopic cTopic = new NewTopic(topic, 2, (short) 1);
		CreateTopicsResult createTopicsResult = adminClient.createTopics(Arrays.asList(cTopic));
		createTopicsResult.all().get();
	}	
}
