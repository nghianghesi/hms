package hms.provider;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Inject;

import hms.common.IHMSExecutorContext;
import hms.dto.Provider;
import hms.provider.models.ProviderModel;
import hms.provider.repositories.IProviderRepository;

public class ProviderInitializer implements IProviderInitializingService{	 
	private IProviderRepository repo;
	protected IHMSExecutorContext execContext;
	@Inject
	public ProviderInitializer(IHMSExecutorContext ec, IProviderRepository repo){
		this.repo = repo;
		this.execContext = ec;
	}
	
	@Override
	public CompletableFuture<List<Provider>> loadByZone(String zone) {
		return CompletableFuture.supplyAsync(()->{
			return this.repo.getProvidersByZone(zone).stream()
					.map(p->new hms.dto.Provider(p.getProviderid(), p.getZone(), p.getName()))
					.collect(Collectors.toList());
		}, this.execContext.getExecutor());
	}

	@Override
	public CompletableFuture<Boolean> clearByZone(String zone) {
		return CompletableFuture.supplyAsync(()->{
			this.repo.clearByZone(zone);
			return true;
		}, this.execContext.getExecutor());
	}

	@Override
	public CompletableFuture<Boolean> initprovider(Provider providerdto) {
		return CompletableFuture.supplyAsync(()->{
			ProviderModel provider = this.repo.LoadById(providerdto.getProviderid());
			if(provider == null) {
				provider = new ProviderModel();			
			}
			provider.load(providerdto);
			this.repo.Save(provider);
			return true;
		}, this.execContext.getExecutor());
	}

}
