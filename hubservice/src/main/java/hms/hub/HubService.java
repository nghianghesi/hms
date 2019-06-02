package hms.hub;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hms.common.IHMSExecutorContext;
import hms.dto.GeoQuery;
import hms.hub.models.HubNodeModel;
import hms.hub.repositories.IHubNodeRepository;

public class HubService implements IHubService, IHubServiceProcessor {		
	private static final Logger logger = LoggerFactory.getLogger(HubService.class);

	private HubNodeModel rootNode;
	private IHMSExecutorContext execContext;
	private IHubNodeRepository repo;
	@Inject
	public HubService(IHMSExecutorContext ec, IHubNodeRepository repo) {
		this.rootNode = repo.getRootNode();
		logger.info(this.rootNode.getDebugInfo());
		this.execContext = ec;
		this.repo = repo;
	}

	public CompletableFuture<UUID> asynGetHostingHubId(double latitude, double longitude)
	{
		return CompletableFuture.supplyAsync(()->{
			return this.getHostingHubId(latitude, longitude);
		}, this.execContext.getExecutor());
	}
	
	public CompletableFuture<List<UUID>> asynGetConveringHubs(hms.dto.GeoQuery query)
	{
		return CompletableFuture.supplyAsync(()->{
			return this.getConveringHubs(query);
		}, this.execContext.getExecutor());	
	}

	@Override
	public UUID getHostingHubId(double latitude, double longitude) {
		return this.rootNode.getHostingHub(latitude, longitude).getHubid();
	}

	@Override
	public List<UUID> getConveringHubs(GeoQuery query) {
		return this.rootNode.getConveringHubIds(query.getLatitude(), query.getLongitude(), query.getDistance()).stream()
		.map(h->h.getHubid()).collect(Collectors.toList());
	}
	
	public void split(UUID id, double subrange) {
		this.rootNode.split(id, subrange);
		this.repo.saveRootNode(this.rootNode);
	}
}
