package hms;

import com.google.inject.AbstractModule;

public class DirectModule extends AbstractModule {
	@Override
	protected void configure() {
        bind(hms.provider.repositories.IProviderRepository.class).to(hms.provider.repositories.ProviderRepository.class);
        bind(hms.provider.IProviderService.class).to(hms.provider.ProviderService.class);        
	}
}
