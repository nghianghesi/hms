package hms.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import hms.kafka.provider.InMemoryHubProviderTrackingProcessing;
import hms.provider.repositories.IProviderRepository;
import hms.provider.repositories.ProviderRepository;
import play.inject.ApplicationLifecycle;

public class KafkaInMemHubProviderConsumerModule extends AbstractModule{
	public static class InMemoryProviderTrackingWithHubProcessingProvider 
		extends hms.commons.KafkaProcessingProvider<InMemoryHubProviderTrackingProcessing> {

		@Inject
		public InMemoryProviderTrackingWithHubProcessingProvider(ApplicationLifecycle app, Injector injector) {
			super(app, injector);
		}

		@Override
		protected InMemoryHubProviderTrackingProcessing internalGet() {
			return new InMemoryHubProviderTrackingProcessing(
					this.injector.getInstance(Config.class),
					this.injector.getInstance(hms.common.IHMSExecutorContext.class),
					this.injector.getInstance(IProviderRepository.class));
		}
	}	
	
	@Override
	protected void configure() {		
		bind(IProviderRepository.class).to(ProviderRepository.class);
        bind(InMemoryHubProviderTrackingProcessing.class)
        	.toProvider(InMemoryProviderTrackingWithHubProcessingProvider.class)
        	.asEagerSingleton();        
	}
}
