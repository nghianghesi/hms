/*package hms.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import hms.kafka.provider.KafkaProviderProcessing;
import play.inject.ApplicationLifecycle;

public class KafkaConsumerModule extends AbstractModule {
	
	private static class KafkaProviderProcessingProvider extends hms.commons.KafkaProcessingProvider<KafkaProviderProcessing> {

		@Inject
		public KafkaProviderProcessingProvider(ApplicationLifecycle app, Injector injector) {
			super(app, injector);
		}

		@Override
		protected KafkaProviderProcessing internalGet() {
			return new KafkaProviderProcessing(
					this.injector.getInstance(Config.class),
					this.injector.getInstance(IProviderService.class),
					this.injector.getInstance(hms.common.IHMSExecutorContext.class));
		}
	}	
	
	@Override
	protected void configure() {        
		bind(hms.hub.repositories.IHubNodeRepository.class).to(hms.hub.repositories.HubNodeRepository.class);
        bind(hms.provider.repositories.IProviderRepository.class).to(hms.provider.repositories.ProviderRepository.class);
        bind(hms.provider.IProviderService.class).to(hms.provider.ProviderService.class);
        bind(hms.hub.IHubService.class).to(hms.hub.HubService.class);
        bind(hms.kafka.provider.KafkaProviderProcessing.class)
        	.toProvider(KafkaProviderProcessingProvider.class)
        	.asEagerSingleton();
	}
}*/
