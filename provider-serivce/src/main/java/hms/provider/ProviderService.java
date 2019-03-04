package hms.provider;

import java.security.InvalidKeyException;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hms.dto.ProviderTracking;
import hms.hub.IHubService;
import hms.hub.IProviderService;
import hms.provider.models.ProviderModel;
import hms.provider.models.ProviderModel.ProviderTrackingModel;
import hms.provider.repositories.IProviderRepository;

public class ProviderService implements IProviderService{    
	private static final Logger logger = LoggerFactory.getLogger(ProviderService.class);

	private IProviderRepository repo;
	private IHubService hubservice;
	
	@Inject
	public ProviderService(IHubService hubservice, IProviderRepository repo){
		this.repo = repo;
		this.hubservice = hubservice;
	}
	
	@Override
	public void clear() {
		this.repo.clear();
		
	}

	@Override
	public void initprovider(hms.dto.Provider providerdto) {
		logger.info("Provider dto:" + providerdto.getProviderid().toString());
		ProviderModel provider = this.repo.LoadById(providerdto.getProviderid());
		if(provider == null) {
			provider = new ProviderModel();			
		}
		provider.load(providerdto);
		this.repo.Save(provider);
	}
	
	@Override
	public void tracking(ProviderTracking trackingdto) throws InvalidKeyException {
		hms.provider.models.ProviderModel provider = this.repo.LoadById(trackingdto.getProviderid());
		if(provider == null) {
			throw new InvalidKeyException(String.format("Provider not found {0}", trackingdto.getProviderid()));
		}
		UUID hubid = this.hubservice.getHostingHubId(trackingdto.getLatitude(), trackingdto.getLongitude());
		ProviderTrackingModel tracking = new ProviderTrackingModel(hubid, trackingdto.getLatitude(),trackingdto.getLongitude());
		provider.setCurrentTracking(tracking);
		this.repo.Save(provider);
	}
}
