package hms.provider;

import com.google.inject.AbstractModule;

public class DirectModule extends AbstractModule {
	@Override
	protected void configure() {		
        bind(hms.hub.repositories.IHubNodeRepository.class).to(hms.hub.repositories.HubNodeRepository.class);        
        bind(hms.hub.IHubService.class).to(hms.hub.HubService.class);
        
        bind(hms.provider.repositories.IProviderRepository.class).to(hms.provider.repositories.ProviderRepository.class);
        bind(hms.provider.IProviderService.class).to(hms.provider.ProviderService.class);        
	}
}
