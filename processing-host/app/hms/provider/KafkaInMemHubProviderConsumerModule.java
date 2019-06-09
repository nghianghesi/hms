package hms.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import hms.kafka.provider.InMemoryProviderProcessing;
import hms.provider.repositories.IProviderRepository;
import hms.provider.repositories.ProviderRepository;
import play.inject.ApplicationLifecycle;

public class KafkaInMemHubProviderConsumerModule extends AbstractModule{
	public static class InMemoryProviderTrackingWithHubProcessingProvider 
		extends hms.commons.KafkaProcessingProvider<InMemoryProviderProcessing> {

		@Inject
		public InMemoryProviderTrackingWithHubProcessingProvider(ApplicationLifecycle app, Injector injector) {
			super(app, injector);
		}

		@Override
		protected InMemoryProviderProcessing internalGet() {
			return new InMemoryProviderProcessing(
					this.injector.getInstance(Config.class),
					this.injector.getInstance(IProviderRepository.class));
		}
	}	
	
	@Override
	protected void configure() {		
		bind(IProviderRepository.class).to(ProviderRepository.class);
        bind(InMemoryProviderProcessing.class)
        	.toProvider(InMemoryProviderTrackingWithHubProcessingProvider.class)
        	.asEagerSingleton();        
	}
}
