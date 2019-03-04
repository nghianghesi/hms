package hms.hub;

import java.util.UUID;

import javax.inject.Inject;

import hms.hub.models.HubNodeModel;
import hms.hub.repositories.IHubNodeRepository;

public class HubService implements IHubService {	
	private HubNodeModel rootNode;
	@Inject
	public HubService(IHubNodeRepository repo) {
		this.rootNode = repo.getRootNode();
	}

	public UUID getHostingHubId(double latitude, double longitude)
	{
		return this.rootNode.getHostingHub(latitude, longitude).getHubid();
	}
}
