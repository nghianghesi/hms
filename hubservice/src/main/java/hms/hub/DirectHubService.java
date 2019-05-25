package hms.hub;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import hms.dto.CoveringHubsResponse;
import hms.dto.GeoQuery;

public class DirectHubService implements IHubService{

	private final UUID onlyhubid = UUID.fromString("eca3229d-c616-491a-aee6-44bcd902cb1b");	
	private final CoveringHubsResponse onlyConverHub = new CoveringHubsResponse();
	public DirectHubService() {
		onlyConverHub.add(onlyhubid);
	}

	@Override
	public CompletableFuture<UUID> getHostingHubId(double latitude, double longitude) {
		return CompletableFuture.supplyAsync(()->onlyhubid);
	}

	@Override
	public CompletableFuture<CoveringHubsResponse> getConveringHubs(GeoQuery query) {
		return CompletableFuture.supplyAsync(()->onlyConverHub);
	}

}
