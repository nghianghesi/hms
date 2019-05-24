package hms.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import hms.kafka.provider.InMemoryProviderTrackingWithHubProcessing;
import hms.provider.repositories.IProviderRepository;
import hms.provider.repositories.ProviderRepository;
import play.inject.ApplicationLifecycle;

public class KafkaInMemHubProviderConsumerModule extends AbstractModule{
	public static class InMemoryProviderTrackingWithHubProcessingProvider 
		extends hms.commons.KafkaProcessingProvider<InMemoryProviderTrackingWithHubProcessing> {

		@Inject
		public InMemoryProviderTrackingWithHubProcessingProvider(ApplicationLifecycle app, Injector injector) {
			super(app, injector);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected InMemoryProviderTrackingWithHubProcessing internalGet() {
			return new InMemoryProviderTrackingWithHubProcessing(
					this.injector.getInstance(Config.class),
					this.injector.getInstance(hms.common.IHMSExecutorContext.class),
					this.injector.getInstance(IProviderRepository.class));
		}
	}	
	
	@Override
	protected void configure() {		
		bind(IProviderRepository.class).to(ProviderRepository.class);
        bind(InMemoryProviderTrackingWithHubProcessing.class)
        	.toProvider(InMemoryProviderTrackingWithHubProcessingProvider.class)
        	.asEagerSingleton();        
	}
}
