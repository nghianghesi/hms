package hms.provider;

import com.google.inject.AbstractModule;

public class DirectModule extends AbstractModule {
	@Override
	protected void configure() {		
        bind(hms.hub.IHubService.class).to(hms.hub.DirectHubService.class);
        
        bind(hms.provider.repositories.IProviderRepository.class).to(hms.provider.repositories.ProviderRepository.class);
        bind(hms.provider.IAsynProviderService.class).to(hms.provider.ProviderService.class);        
	}
}
