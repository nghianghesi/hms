package hms.provider;

import java.util.concurrent.CompletableFuture;

public interface IProviderService {
	public final String ClearMessage = "clear";
	public final String InitproviderMessage  = "initprovider";
	public final String TrackingMessage  = "tracking";
	CompletableFuture<Boolean> clear();
	CompletableFuture<Boolean> initprovider(hms.dto.Provider providerdto);
	CompletableFuture<Boolean> tracking(hms.dto.ProviderTracking trackingdto);
}

