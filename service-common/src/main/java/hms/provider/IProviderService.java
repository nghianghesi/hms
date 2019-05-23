package hms.provider;

import java.util.concurrent.CompletableFuture;

public interface IProviderService {
	CompletableFuture<Boolean> tracking(hms.dto.ProviderTracking trackingdto);
	CompletableFuture<hms.dto.ProvidersGeoQueryResponse> queryProviders(hms.dto.GeoQuery query);
}

