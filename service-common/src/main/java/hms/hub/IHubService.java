package hms.hub;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IHubService {

	public final String MappingHubMessage = "mapping-hub";
	CompletableFuture<UUID> getHostingHubId(double latitude, double longitude);

}