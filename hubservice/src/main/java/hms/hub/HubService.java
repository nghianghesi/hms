package hms.hub;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import hms.hub.models.HubNodeModel;
import hms.hub.repositories.IHubNodeRepository;

public class HubService implements IHubService {	
	private HubNodeModel rootNode;
	@Inject
	public HubService(IHubNodeRepository repo) {
		this.rootNode = repo.getRootNode();
	}

	public CompletableFuture<UUID> getHostingHubId(double latitude, double longitude)
	{
		return CompletableFuture.supplyAsync(()->{
			//TODO: need return hubid full-path
			return this.rootNode.getHostingHub(latitude, longitude).getHubid();
		});
	}
}
