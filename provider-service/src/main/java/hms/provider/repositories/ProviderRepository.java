package hms.provider.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.*;
import java.util.UUID;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import hms.provider.entities.ProviderEntity;
import hms.provider.entities.ProviderTrackingEntity;
import hms.provider.models.ProviderModel;
import xyz.morphia.Datastore;
import xyz.morphia.FindAndModifyOptions;
import xyz.morphia.Morphia;
import xyz.morphia.query.Query;
import xyz.morphia.query.UpdateOperations;


public class ProviderRepository implements IProviderRepository {
	private static final Logger logger = LoggerFactory.getLogger(ProviderRepository.class);

	private Datastore datastore;
	private Morphia morphia;
	@Inject
	public ProviderRepository(Morphia morphia, Datastore datastore) {
		this.datastore=datastore;
		this.morphia = morphia;
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
	
	@Override
	public void clearByZone(String name) {
		DBCollection collection = datastore.getCollection(ProviderEntity.class);
        if(collection!=null) {
        	BasicDBObject document = new BasicDBObject().append("zone", name);
        	collection.remove(document);
        }
	}
	
	@Override
	public List<hms.provider.models.ProviderModel> getProvidersByIds(List<UUID> providerids){
		if(providerids.size()>0) {
			return this.datastore.createQuery(ProviderEntity.class)
					.field("providerid").in(providerids).asList()
					.stream().map(e -> ProviderModel.load(e)).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
	
	@Override
	public List<hms.provider.models.ProviderModel> getProvidersByZone(String zone){
		logger.info("getProvidersByZone {}", zone);
		return this.datastore.createQuery(ProviderEntity.class)
				.field("zone").equal(zone).asList()
				.stream().map(e -> ProviderModel.load(e)).collect(Collectors.toList());
	}
	
	
	@Override
	public List<hms.provider.models.ProviderModel> geoSearchProviders(List<UUID> hostids, double latitude, double longitude, int distance) {
		if(hostids.size()>0) {
			List<UUID> parsedhostids = new ArrayList<>();
			for(Object h:hostids) {
				if(h.getClass().getName().contains("String")){
					parsedhostids.add(UUID.fromString(h.toString()));
				}else {
					parsedhostids.add((UUID)h);
				}
			}

			DBObject query = BasicDBObjectBuilder.start()
					.add("hubid", new BasicDBObject("$in", parsedhostids))
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
			}
			
			if(providerids.size()>0) {
				return this.datastore.createQuery(ProviderEntity.class)
						.field("providerid").in(providerids).asList()
						.stream().map(e -> ProviderModel.load(e)).collect(Collectors.toList());
			}
		}
		return new ArrayList<>();
	}
}


