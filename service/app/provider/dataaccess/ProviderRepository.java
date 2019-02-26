package provider.dataaccess;

import java.util.UUID;
import javax.inject.Inject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import commons.HMSPlayMorphia;
import it.unifi.cerm.playmorphia.PlayMorphia;
import provider.models.ProviderTracking;

public class ProviderRepository implements IProviderRepository {
	
	private PlayMorphia morphia;
	@Inject
	public ProviderRepository(HMSPlayMorphia morphia) {
		this.morphia=morphia;
	}	

	@Override
	public ProviderTracking LoadById(UUID id){
		if(id!=null) {
			return this.InternalLoadById(id);
		}else {
			return null;
		}
	}
	
	@Override
	public void Save(ProviderTracking tracking) {
        morphia.datastore().save(tracking);               
	}	
	
	@Override
	public void clear() {
		DBCollection collection=morphia.datastore().getCollection(ProviderTracking.class);
        if(collection!=null) {
        	BasicDBObject document = new BasicDBObject();
        	collection.remove(document);
        }
	}
	
	private provider.models.ProviderTracking InternalLoadById(UUID id){
		return this.morphia.datastore()
					.createQuery(ProviderTracking.class).filter("providerid == ", id)
					.get();
		
	}
}
