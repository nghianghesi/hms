package hms.provider;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import hms.dto.ProviderTracking;
import hms.provider.repositories.IProviderRepository;

public class ProviderServiceProcessor extends ProviderService implements IProviderServiceProcessor{

	public ProviderServiceProcessor(IProviderRepository repo) {
		super(null, repo);
	}	

	@Override
	public CompletableFuture<Boolean> asynTracking(ProviderTracking trackingdto) {
		throw new Error("Invalid usage, don't call this directly");
	}

	@Override
	public Boolean tracking(ProviderTracking trackingdto, UUID hubid) {
		return this.internalTrackingProviderHub(trackingdto, hubid);
	}
	
	@Override
	public CompletableFuture<List<hms.dto.Provider>> asynQueryProviders(hms.dto.GeoQuery query){
		throw new Error("Invalid usage, don't call this directly");
	}
	
	@Override
	public List<hms.dto.Provider> queryProviders(List<UUID>hostids, hms.dto.GeoQuery query){
		return this.internalQueryProviders(hostids, query);
	}
}
