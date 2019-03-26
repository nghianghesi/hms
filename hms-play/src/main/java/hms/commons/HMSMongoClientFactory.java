package hms.commons;

import com.mongodb.MongoClient;
import com.typesafe.config.Config;
import it.unifi.cerm.playmorphia.MongoClientFactory;

public class HMSMongoClientFactory extends MongoClientFactory  {

    public HMSMongoClientFactory(Config config) {
        super(config);
        this.config = config;        
    }

    public MongoClient createClient() throws Exception {
    	MongoClient client= new MongoClient(getClientURI());    
    	
    	return client;
     }
}
