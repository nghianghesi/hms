package hms.hub.repositories;

import javax.inject.Inject;
import hms.hub.entities.HubNodeEntity;
import hms.hub.models.HubNodeModel;
import xyz.morphia.Datastore;

public class HubNodeRepository implements IHubNodeRepository {
	private Datastore datastore;
	@Inject
	public HubNodeRepository(Datastore morphia) {
		this.datastore = morphia;
	}	
	
	public HubNodeModel getRootNode() {
		HubNodeEntity entity = this.datastore.createQuery(HubNodeEntity.class).get();
		if(entity == null) {
			HubNodeModel root = new HubNodeModel();
	        datastore.save(root.persistance());           
			return root;
		}else {
			return new HubNodeModel(entity);
		}
	}
}
