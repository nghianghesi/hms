package hms.provider;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IProviderServiceProcessor{		
	CompletableFuture<Boolean> clear();
	CompletableFuture<Boolean> initprovider(hms.dto.Provider providerdto);
	CompletableFuture<Boolean> tracking(hms.dto.ProviderTracking trackingdto, UUID hubid);
}
