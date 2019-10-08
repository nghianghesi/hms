package hms.hub.repositories;

import hms.hub.models.HubNodeModel;

public interface IHubNodeRepository {
	HubNodeModel getRootNode();
	
	void saveRootNode(HubNodeModel node);
}
