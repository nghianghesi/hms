package hms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

import hms.ProviderTrackingEntity;
import xyz.morphia.Datastore;
import xyz.morphia.Morphia;

public class Client {
    public static Logger log = LoggerFactory.getLogger( Client.class );

    private static MongoClient        mongodb;
    private static Morphia      morphia;
    private static Datastore    datastore;

    private static final String DATABASE_NAME   = "hms";

    public static Mongo getMongo() { return mongodb; }
    public static Morphia getMorphia() { return morphia; }
    public static Datastore getDatastore() { return datastore; }

    /**
     * Initialize MongoDB and Morphia for our purposes.
     */
    private static void initializeMongoAndMorphia()
    {
        try
        {
            mongodb = new MongoClient( "localhost", 27017 );
        }
        catch( MongoException e )
        {
            log.error( "Error attempting MongoDB connection for hotels", e );
        }

        morphia   = new Morphia();
        morphia.map(ProviderTrackingEntity.class);
        datastore = morphia.createDatastore( mongodb, DATABASE_NAME );
        
    }
	public static void maindb(String[] args) {
		initializeMongoAndMorphia();

		double longitude =-118.01358499633821;
		double latitude = 33.99110972129109;
		int distance = 10000;
		List<UUID>hostids = new ArrayList<UUID>(); 
		hostids.add(UUID.fromString("3f4f7223-4ccd-2d25-9c39-cb49794b53a7"));
		hostids.add(UUID.fromString("eca3229d-c616-491a-aee6-44bcd902cb1b"));

			DBObject query = BasicDBObjectBuilder.start()
					.add("hubid", new BasicDBObject("$in", hostids))
					.add("location", 
							new BasicDBObject("$near", 
									new BasicDBObject("$geometry", 
											BasicDBObjectBuilder.start()
												.add("type", "Point")
												.add("coordinates", new double[] {longitude, latitude})
												.add("$maxDistance", distance).get())))
					.get();
		
		List<UUID> providerids = new ArrayList<>();
		DBCursor dbCursor = datastore.getCollection(ProviderTrackingEntity.class).find(query);
		while (dbCursor.hasNext()) {
		    DBObject obj = dbCursor.next();
		    ProviderTrackingEntity t = morphia.fromDBObject(datastore, ProviderTrackingEntity.class, obj);
		    providerids.add(t.getProviderid());
			System.out.println("found tracking"+ t.getProviderid());
		}
		

		System.out.println(providerids);
	}
	
	public static void main(String[] args) {
		Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
		List<String> keys = new ArrayList<>();
		for(int i=0;i<1000;i++) {
			String t=UUID.randomUUID().toString(); 
			map.put(t, true);
			keys.add(t);
		}
		int idx = 0;
		for(String t: map.keySet().toArray(new String[] {})) {
			System.out.println(t+" "+keys.get(idx));
			idx++;			
			map.remove(t);
		}
	}
}










