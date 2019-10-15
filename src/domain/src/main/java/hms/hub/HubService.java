package hms.hub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import hms.dto.GeoQuery;
import hms.dto.HubDTO;
import hms.hub.models.HubNodeModel;
import hms.hub.repositories.IHubNodeRepository;

public class HubService implements IHubService, IHubServiceProcessor {		
	private static final Logger logger = LoggerFactory.getLogger(HubService.class);

	private HubNodeModel rootNode;
	private Map<UUID,HubNodeModel> hubMap = new HashMap<UUID,HubNodeModel>();

	@Autowired
	private IHubNodeRepository repo;

	@PostConstruct
	public void InitHubService() {
		this.rootNode = repo.getRootNode();
		logger.info(this.rootNode.getDebugInfo());
		this.rootNode.collectNodes(this.hubMap);
	}

	public CompletableFuture<UUID> asynGetHostingHubId(double latitude, double longitude)
	{
		return CompletableFuture.supplyAsync(()->{
			return this.getHostingHubId(latitude, longitude);
		});
	}
	
	public CompletableFuture<List<UUID>> asynGetConveringHubs(hms.dto.GeoQuery query)
	{
		return CompletableFuture.supplyAsync(()->{
			return this.getConveringHubs(query);
		});	
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
	
	public void split(UUID id, int parts) {
		this.rootNode.split(id, parts);
		this.repo.saveRootNode(this.rootNode);
	}

	@Override
	public String getZone(UUID hubid) {
		HubNodeModel hub = this.hubMap.getOrDefault(hubid, null);
		if(hub!=null && hub.getZone() != null && !hub.getZone().equals("")) {
			return hub.getZone();
		}
		return "none";
	}
	
	public HubDTO getRootHub() {
		return getHubTree(this.rootNode);
	}
	
	private HubDTO getHubTree(HubNodeModel hub) {
		List<HubDTO> subDTOs = new ArrayList<HubDTO>();
		for(HubNodeModel subhub:hub.getSubHubs()) {
			subDTOs.add(getHubTree(subhub));
		}
		return HubDTO.createByIdNameZoneActiveSubs(hub.getHubid(), hub.getName(), hub.getZone(), hub.getIsActive(), subDTOs);
	}
	
	private boolean findAndEnable(HubNodeModel hub, UUID hubid) {
		if(hub.getHubid().equals(hubid)) {
			hub.setIsActive(true);
			return true;
		}			
		
		for(HubNodeModel sub : hub.getSubHubs()) {
			if(this.findAndEnable(sub, hubid)){
				return true;
			}
		}
		return false;
	}

	public void enable(UUID hubid) {
		if(this.rootNode.getHubid().equals(hubid)) {
			return;
		}

		this.findAndEnable(this.rootNode, hubid);
		this.repo.saveRootNode(this.rootNode);
	}
	
	private void disable (HubNodeModel hub) {
		if(hub.getIsActive()) {
			hub.setIsActive(false);
			for(HubNodeModel sub : hub.getSubHubs()) {
				this.disable(sub);
			}
		}
	}
	
	public void disable(UUID hubid) {
		if(this.rootNode.getHubid().equals(hubid)) {
			return;
		}		
		
		if(this.hubMap.containsKey(hubid)) {
			HubNodeModel hub = this.hubMap.get(hubid);
			this.disable(hub);
			this.repo.saveRootNode(this.rootNode);
		}
	}
	
}
