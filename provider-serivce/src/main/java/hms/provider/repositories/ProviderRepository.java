package hms.provider.repositories;

import java.util.UUID;
import javax.inject.Inject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import hms.provider.ProviderPlayMorphia;
import hms.provider.models.ProviderModel;
import it.unifi.cerm.playmorphia.PlayMorphia;

public class ProviderRepository implements IProviderRepository {
	
	private PlayMorphia morphia;
	@Inject
	public ProviderRepository(ProviderPlayMorphia morphia) {
		this.morphia=morphia;
	}	

	@Override
	public ProviderModel LoadById(UUID id){
		if(id!=null) {
			return this.InternalLoadById(id);
		}else {
			return null;
		}
	}
	
	@Override
	public void Save(hms.provider.entities.ProviderEntity provider) {
        morphia.datastore().save(provider);               
	}	
	
	@Override
	public void clear() {
		DBCollection collection = morphia.datastore().getCollection(ProviderModel.class);
        if(collection!=null) {
        	BasicDBObject document = new BasicDBObject();
        	collection.remove(document);
        }
	}
	
	private hms.provider.models.ProviderModel InternalLoadById(UUID id){
		return this.morphia.datastore()
					.createQuery(ProviderModel.class).filter("providerid == ", id)
					.get();
		
	}
}
