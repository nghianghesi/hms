package hms.provider;

import java.security.InvalidKeyException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;

import hms.common.ExceptionWrapper;
import hms.common.IHMSExecutorContext;
import hms.dto.GeoQuery;
import hms.dto.Provider;
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
	public CompletableFuture<Boolean> asynTracking(ProviderTracking trackingdto) {
		return this.hubservice.asynGetHostingHubId(trackingdto.getLatitude(), trackingdto.getLongitude())
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
	public CompletableFuture<List<hms.dto.Provider>> asynQueryProviders(hms.dto.GeoQuery query){
		return this.hubservice.asynGetConveringHubs(query)
			.thenApplyAsync((hubids) -> {
				return this.internalQueryProviders(hubids, query);
			},this.execContext.getExecutor());	
	}

	@Override
	public Boolean tracking(ProviderTracking trackingdto) {
		return this.internalTrackingProviderHub(trackingdto, 
				this.hubservice.getHostingHubId(trackingdto.getLatitude(), trackingdto.getLongitude()));
	}

	@Override
	public List<Provider> queryProviders(GeoQuery query) {
		return this.internalQueryProviders(this.hubservice.getConveringHubs(query),query);
	}
}
