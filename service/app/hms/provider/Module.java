package hms.provider;

import com.google.inject.AbstractModule;

public class Module extends AbstractModule {
	@Override
	protected void configure() {
        bind(hms.provider.repositories.IProviderRepository.class).to(hms.provider.repositories.ProviderRepository.class).asEagerSingleton();
        //bind(hms.provider.ProviderPlayMorphia.class).to(hms.provider.ProviderPlayMorphia.class).asEagerSingleton();
        bind(hms.hub.IProviderService.class).to(hms.provider.ProviderService.class).asEagerSingleton();        
	}
}
