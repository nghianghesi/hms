package hms.hub;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IHubService {

	CompletableFuture<UUID> getHostingHubId(double latitude, double longitude);

}