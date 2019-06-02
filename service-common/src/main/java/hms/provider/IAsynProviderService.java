package hms.provider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import hms.dto.Provider;

public interface IAsynProviderService {
	CompletableFuture<Boolean> asynTracking(hms.dto.ProviderTracking trackingdto);
	CompletableFuture<? extends List<Provider>> asynQueryProviders(hms.dto.GeoQuery query);
}
