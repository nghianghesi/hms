package hms.provider;

import com.google.inject.AbstractModule;

public class KafkaProducerModule extends AbstractModule {
	@Override
	protected void configure() {
        bind(hms.provider.IAsynProviderService.class)
        	.toProvider(KafkaProducerServiceProvider.class).asEagerSingleton();        
	}
}
