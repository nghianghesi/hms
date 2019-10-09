package hms.hub.repositories;

import hms.hub.entities.HubRootEntity;
import hms.hub.models.HubNodeModel;
import org.springframework.beans.factory.annotation.Autowired;
import dev.morphia.Datastore;

public class HubNodeRepository implements IHubNodeRepository {
	@Autowired
	private Datastore datastore;
	
	public HubNodeModel getRootNode() {
		HubRootEntity entity = this.datastore.createQuery(HubRootEntity.class).first();
		if(entity == null) {
			HubNodeModel root = new HubNodeModel();
	        datastore.save(root.persistance());           
			return root;
		}else {
			return new HubNodeModel(entity);
		}
	}
	
	public void saveRootNode(HubNodeModel node) {
		datastore.save(node.persistance());   
	}
}
