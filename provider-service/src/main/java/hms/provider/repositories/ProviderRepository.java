package hms.provider.repositories;

import java.util.UUID;
import javax.inject.Inject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

import hms.provider.entities.ProviderEntity;
import hms.provider.entities.ProviderTrackingEntity;
import hms.provider.models.ProviderModel;
import xyz.morphia.Datastore;
import xyz.morphia.FindAndModifyOptions;
import xyz.morphia.query.Query;
import xyz.morphia.query.UpdateOperations;

public class ProviderRepository implements IProviderRepository {

	private Datastore datastore;
	@Inject
	public ProviderRepository(Datastore datastore) {
		this.datastore=datastore;
	}	

	@Override
	public ProviderModel LoadById(UUID id){
		if(id!=null) {
			ProviderEntity entity = this.datastore
					.createQuery(ProviderEntity.class).field("providerid").equal(id)
					.get();
			return ProviderModel.load(entity);
		}else {
			return null;
		}
	}
	
	private boolean isSameHub(ProviderEntity.ProviderTrackingStruct entity, ProviderEntity.ProviderTrackingStruct other) {
		return entity == other
				|| (entity != null && other!=null && entity.getHubid() == other.getHubid());
	}
	@Override
	public void Save(ProviderModel provider) {
        datastore.save(provider.persistance());
        this.SaveTracking(provider);
    }	
	
	@Override
	public void SaveTracking(ProviderModel provider) {
		ProviderEntity entity = provider.persistance();
		ProviderEntity.ProviderTrackingStruct previousTracking = entity.getPreviousTracking();
		ProviderEntity.ProviderTrackingStruct currentTracking = entity.getCurrentTracking();

		if(!isSameHub(previousTracking, currentTracking)
				&& previousTracking!=null) {
				Query<ProviderTrackingEntity> query = this.datastore
						.createQuery(ProviderTrackingEntity.class).field("hubid").equal(previousTracking.getHubid())
						.field("providerid").equal(provider.getProviderid()); 
				this.datastore.findAndDelete(query);
		}

		if(currentTracking != null) {
			Query<ProviderTrackingEntity> query = this.datastore
					.createQuery(ProviderTrackingEntity.class)
						.field("hubid").equal(currentTracking.getHubid())
						.field("providerid").equal(provider.getProviderid());
			UpdateOperations<ProviderTrackingEntity> update = this.datastore.createUpdateOperations(ProviderTrackingEntity.class)
					.set("location", currentTracking.getLocation());
			FindAndModifyOptions upsert = new FindAndModifyOptions().upsert(true);
			this.datastore.findAndModify(query, update, upsert);
		}
	}

	
	@Override
	public void clear() {
		DBCollection collection = datastore.getCollection(ProviderEntity.class);
        if(collection!=null) {
        	BasicDBObject document = new BasicDBObject();
        	collection.remove(document);
        }		
        
        collection = datastore.getCollection(ProviderTrackingEntity.class);
        if(collection!=null) {
        	BasicDBObject document = new BasicDBObject();
        	collection.remove(document);
        }
	}
}
