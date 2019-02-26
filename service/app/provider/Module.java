package provider;

import com.google.inject.AbstractModule;

import commons.HMSPlayMorphia;


public class Module extends AbstractModule {
	@Override
	protected void configure() {
        bind(provider.dataaccess.IProviderRepository.class).to(provider.dataaccess.ProviderRepository.class).asEagerSingleton();
        //bind(HMSPlayMorphia.class).to(HMSPlayMorphia.class);
	}
}
