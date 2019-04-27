package hms.hub;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IHubServiceProcessor {
	CompletableFuture<UUID> getHostingHubId(double latitude, double longitude);
	public CompletableFuture<hms.dto.CoveringHubsResponse> getConveringHubs(hms.dto.GeoQuery query);	
}
