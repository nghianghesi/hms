package hms.provider;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import hms.common.IHMSExecutorContext;
import hms.dto.ProviderTracking;
import hms.provider.repositories.IProviderRepository;

public class ProviderServiceProcessor extends ProviderService implements IProviderServiceProcessor{

	@Inject
	public ProviderServiceProcessor(IHMSExecutorContext ec, IProviderRepository repo) {
		super(ec, null, repo);
	}	

	@Override
	public CompletableFuture<Boolean> tracking(ProviderTracking trackingdto) {
		throw new Error("Invalid usage, don't call this directly");
	}

	@Override
	public CompletableFuture<Boolean> tracking(ProviderTracking trackingdto, UUID hubid) {
		return CompletableFuture.supplyAsync(()->{
			return this.internalTrackingProviderHub(trackingdto, hubid);
		}, this.execContext.getExecutor());
	}
	
	@Override
	public CompletableFuture<List<hms.dto.Provider>> queryProviders(hms.dto.Coordinate position, double distance){
		throw new Error("Invalid usage, don't call this directly");
	}
	
	@Override
	public CompletableFuture<List<hms.dto.Provider>> queryProviders(List<UUID>hostids, hms.dto.Coordinate position, double range){
		return CompletableFuture.supplyAsync(()->{
			return this.internalQueryProviders(hostids, position, range);
		}, this.execContext.getExecutor());
	}
}
