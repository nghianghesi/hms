package hms.hub;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import hms.common.IHMSExecutorContext;
import hms.hub.models.HubNodeModel;
import hms.hub.repositories.IHubNodeRepository;

public class HubService implements IHubService, IHubServiceProcessor {	
	private HubNodeModel rootNode;
	private IHMSExecutorContext execContext;
	@Inject
	public HubService(IHMSExecutorContext ec, IHubNodeRepository repo) {
		this.rootNode = repo.getRootNode();
		this.execContext = ec;
	}

	public CompletableFuture<UUID> getHostingHubId(double latitude, double longitude)
	{
		return CompletableFuture.supplyAsync(()->{
			//TODO: need return hubid full-path
			return this.rootNode.getHostingHub(latitude, longitude).getHubid();
		}, this.execContext.getExecutor());
	}
}
