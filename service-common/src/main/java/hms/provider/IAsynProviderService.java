package hms.provider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IAsynProviderService {
	CompletableFuture<Boolean> asynTracking(hms.dto.ProviderTracking trackingdto);
	CompletableFuture<List<hms.dto.Provider>> asynQueryProviders(hms.dto.GeoQuery query);
}
