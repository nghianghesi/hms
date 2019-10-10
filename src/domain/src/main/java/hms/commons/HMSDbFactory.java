package hms.commons;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

import dev.morphia.Datastore;
import dev.morphia.Morphia;

public class HMSDbFactory{
	@Value("${morphia.models}")
	private String models;
	@Value("${morphia.uri}")
	private String uri;
	
	private Datastore datastore;

	
	@PostConstruct
	public void InitHMSDbFactory() {		
		Morphia morphia = new Morphia();
		if(models!=null && models.compareTo("")>0) {
			for(String s : models.split(",")) {
				morphia.mapPackage(s);
			}
			MongoClientURI uri = new MongoClientURI(this.uri);
			MongoClient client = new MongoClient(uri);
			this.datastore = morphia.createDatastore(client, uri.getDatabase());
			datastore.ensureIndexes();		
		}			
	}
	
	public Datastore get() {
		return datastore;
	}

}
