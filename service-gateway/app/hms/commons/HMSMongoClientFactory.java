package hms.commons;

import java.util.Arrays;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.typesafe.config.Config;

import it.unifi.cerm.playmorphia.MongoClientFactory;

public class HMSMongoClientFactory extends MongoClientFactory  {
    private Config config;

    public HMSMongoClientFactory(Config config) {
        super(config);
        this.config = config;        
    }

    public MongoClient createClient() throws Exception {
    	MongoClient client= new MongoClient(Arrays.asList(
                 new ServerAddress(config.getString("playmorphia.host"), config.getInt("playmorphia.port"))
                 )
         );    	
    	return client;
     }

    public String getDBName() {
        return config.getString("playmorphia.database");
    }
}
