package hms.provider;

import java.security.InvalidKeyException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hms.common.ExceptionWrapper;
import hms.common.IHMSExecutorContext;
import hms.dto.ProviderTracking;
import hms.hub.IHubService;
import hms.provider.models.ProviderModel;
import hms.provider.models.ProviderModel.ProviderTrackingModel;
import hms.provider.repositories.IProviderRepository;

public class ProviderService implements IProviderService{    
	private static final Logger logger = LoggerFactory.getLogger(ProviderService.class);

	private IProviderRepository repo;
	private IHubService hubservice;
	private IHMSExecutorContext execContext;
	
	@Inject
	public ProviderService(IHMSExecutorContext ec,IHubService hubservice, IProviderRepository repo){
		this.repo = repo;
		this.hubservice = hubservice;
		this.execContext = ec;
	}
	
	@Override
	public CompletableFuture<Boolean> clear() {
		return CompletableFuture.supplyAsync(() -> {
			this.repo.clear();
			return true;
		},this.execContext.getExecutor());		
	}

	@Override
	public CompletableFuture<Boolean> initprovider(hms.dto.Provider providerdto) {
		return CompletableFuture.supplyAsync(()->{
			logger.info("Provider dto:" + providerdto.getProviderid().toString());
			ProviderModel provider = this.repo.LoadById(providerdto.getProviderid());
			if(provider == null) {
				provider = new ProviderModel();			
			}
			provider.load(providerdto);
			this.repo.Save(provider);
			return true;
		},this.execContext.getExecutor());
	}	

	protected CompletableFuture<Boolean> internalTrackingProviderHub(ProviderTracking trackingdto, UUID hubid) {		
		return CompletableFuture.supplyAsync(()->{	
			hms.provider.models.ProviderModel provider = this.repo.LoadById(trackingdto.getProviderid());
			if(provider == null) {
				throw ExceptionWrapper.wrap(new InvalidKeyException(String.format("Provider not found {0}", trackingdto.getProviderid())));
			}
			ProviderTrackingModel tracking = new ProviderTrackingModel(hubid, trackingdto.getLatitude(),trackingdto.getLongitude());
			provider.setCurrentTracking(tracking);
			this.repo.Save(provider);
			return true;
		}, this.execContext.getExecutor());
	}	
	
	@Override
	public CompletableFuture<Boolean> tracking(ProviderTracking trackingdto) {
		return CompletableFuture.supplyAsync(()->{		
			UUID hubid;
			try {
				hubid = this.hubservice.getHostingHubId(trackingdto.getLatitude(), trackingdto.getLongitude()).get();
			} catch (InterruptedException | ExceptionWrapper | ExecutionException e) {
				logger.error("provider tracking error", e.getMessage());
				throw ExceptionWrapper.wrap(e);
			}
			return this.internalTrackingProviderHub(trackingdto, hubid).join();
		}, this.execContext.getExecutor());
	}
}
