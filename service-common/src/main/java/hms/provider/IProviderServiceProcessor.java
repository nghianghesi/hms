package hms.provider;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IProviderServiceProcessor{		
	CompletableFuture<Boolean> clear();
	CompletableFuture<Boolean> initprovider(hms.dto.Provider providerdto);
	CompletableFuture<Boolean> tracking(hms.dto.ProviderTracking trackingdto, UUID hubid);
	CompletableFuture<List<hms.dto.Provider>> queryProviders(List<UUID>hostids, hms.dto.Coordinate position, double range);
}
