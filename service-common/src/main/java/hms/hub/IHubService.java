package hms.hub;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IHubService {
	CompletableFuture<UUID> getHostingHubId(double latitude, double longitude);
	public CompletableFuture<List<UUID>> getConverHubIds(hms.dto.GeoQuery query);	
}