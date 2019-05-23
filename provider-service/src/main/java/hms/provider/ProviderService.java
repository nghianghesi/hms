package hms.provider;

import java.security.InvalidKeyException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;

import hms.common.ExceptionWrapper;
import hms.common.IHMSExecutorContext;
import hms.dto.ProviderTracking;
import hms.hub.IHubService;
import hms.provider.models.ProviderModel.ProviderTrackingModel;
import hms.provider.repositories.IProviderRepository;

public class ProviderService implements IProviderService{    
	private IProviderRepository repo;
	private IHubService hubservice;
	protected IHMSExecutorContext execContext;
	
	@Inject
	public ProviderService(IHMSExecutorContext ec,IHubService hubservice, IProviderRepository repo){
		this.repo = repo;
		this.hubservice = hubservice;
		this.execContext = ec;
	}
	
	protected Boolean internalTrackingProviderHub(ProviderTracking trackingdto, UUID hubid) {		
		hms.provider.models.ProviderModel provider = this.repo.LoadById(trackingdto.getProviderid());
		if(provider == null) {
			throw ExceptionWrapper.wrap(new InvalidKeyException(String.format("Provider not found {0}", trackingdto.getProviderid())));
		}
		ProviderTrackingModel tracking = new ProviderTrackingModel(hubid, trackingdto.getLatitude(),trackingdto.getLongitude());
		provider.setCurrentTracking(tracking);
		this.repo.Save(provider);
		return true;
	}	
	
	@Override
	public CompletableFuture<Boolean> tracking(ProviderTracking trackingdto) {
		return this.hubservice.getHostingHubId(trackingdto.getLatitude(), trackingdto.getLongitude())
					.thenApplyAsync((hubid) -> {
						return this.internalTrackingProviderHub(trackingdto, hubid);			
					}, this.execContext.getExecutor());
	}
	
	
	protected List<hms.dto.Provider> internalQueryProviders(List<UUID> hubids, hms.dto.GeoQuery query){
		return this.repo.geoSearchProviders(hubids, query.getLatitude(), query.getLongitude(), query.getDistance())
		.stream().map(p -> new hms.dto.Provider(p.getProviderid(), p.getZone(), p.getName()))
		.collect(Collectors.toList());
	}
	
	@Override 
	public CompletableFuture<hms.dto.ProvidersGeoQueryResponse> queryProviders(hms.dto.GeoQuery query){
		return this.hubservice.getConveringHubs(query)
			.thenApplyAsync((hubids) -> {
				hms.dto.ProvidersGeoQueryResponse res = new hms.dto.ProvidersGeoQueryResponse(); 
				res.addAll(this.internalQueryProviders(hubids, query));
				return res;
			},this.execContext.getExecutor());	
	}
}
