package hms.hub;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import hms.dto.GeoQuery;

public class DirectHubService implements IHubService{

	private final UUID onlyhubid = UUID.fromString("eca3229d-c616-491a-aee6-44bcd902cb1b");	
	private final List<UUID> onlyConverHub = new ArrayList<UUID>();
	
	@Inject
	public DirectHubService() {
		onlyConverHub.add(onlyhubid);
	}

	@Override
	public CompletableFuture<UUID> asynGetHostingHubId(double latitude, double longitude) {
		return CompletableFuture.supplyAsync(()->onlyhubid);
	}

	@Override
	public CompletableFuture<List<UUID>> asynGetConveringHubs(GeoQuery query) {
		return CompletableFuture.supplyAsync(()->onlyConverHub);
	}

	@Override
	public UUID getHostingHubId(double latitude, double longitude) {
		return onlyhubid;
	}

	@Override
	public List<UUID> getConveringHubs(GeoQuery query) {
		return onlyConverHub;
	}

	@Override
	public void split(UUID id, int parts) {
		return;
		
	}

	@Override
	public String getZone(UUID hubid) {
		return "none";
	}

	public void enable(UUID hubid) {
		return;
	}
	public void disable(UUID hubid) {
		return;
	}
}
