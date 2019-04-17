package hms.hub;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hms.common.IHMSExecutorContext;
import hms.hub.models.HubNodeModel;
import hms.hub.repositories.IHubNodeRepository;

public class HubService implements IHubService, IHubServiceProcessor {		
	private static final Logger logger = LoggerFactory.getLogger(HubService.class);

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
			logger.info("host mapping:{} - {} {}" + this.rootNode.getDebugInfo(), longitude, latitude);
			return this.rootNode.getHostingHub(latitude, longitude).getHubid();
		}, this.execContext.getExecutor());
	}
}
