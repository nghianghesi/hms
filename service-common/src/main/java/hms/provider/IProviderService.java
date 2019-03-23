package hms.provider;

import java.util.concurrent.CompletableFuture;

public interface IProviderService {
	CompletableFuture<Boolean> clear();
	CompletableFuture<Boolean> initprovider(hms.dto.Provider providerdto);
	CompletableFuture<Boolean> tracking(hms.dto.ProviderTracking trackingdto);
}

