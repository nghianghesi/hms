package controllers;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClientURI;
import com.typesafe.config.Config;

import hms.kafka.provider.KafkaProviderService;
import play.mvc.*;
import xyz.morphia.Datastore;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
		Properties props = new Properties();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		AdminClient adminClient = AdminClient.create(props);

		NewTopic cTopic = new NewTopic("test-topic", 2, (short) 1);
		CreateTopicsResult createTopicsResult = adminClient.createTopics(Arrays.asList(cTopic));
		String msg = "";
		try {
			createTopicsResult.all().get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Create topic error", e);
			msg = e.getMessage();
		}    	
		

		Properties props1 = new Properties();
		props1.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		props1.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");
		props1.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.ByteArraySerializer");
		try {
			new KafkaProducer<String, byte[]>(props1);		
		} catch (Exception e) {
			logger.error("Create topic error", e);
			msg += e.getMessage();
		}    		
        return ok(msg + views.html.index.render());
    }

}
