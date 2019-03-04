package hms.hub.repositories;

import javax.inject.Inject;
import hms.hub.HubNodePlayMorphia;
import hms.hub.entities.HubNodeEntity;
import hms.hub.models.HubNodeModel;
import it.unifi.cerm.playmorphia.PlayMorphia;

public class HubNodeRepository implements IHubNodeRepository {
	private PlayMorphia morphia;
	@Inject
	public HubNodeRepository(HubNodePlayMorphia morphia) {
		this.morphia = morphia;
	}	
	
	public HubNodeModel getRootNode() {
		HubNodeEntity entity = this.morphia.datastore().createQuery(HubNodeEntity.class).get();
		if(entity == null) {
			HubNodeModel root = new HubNodeModel();
	        morphia.datastore().save(root.persistance());           
			return root;
		}else {
			return new HubNodeModel(entity);
		}
	}
}
