package hms.hub;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import hms.kafka.hub.KafkaHubProcessing;
import play.inject.ApplicationLifecycle;

public class KafkaConsumerModule extends AbstractModule {
	
	private static class KafkaHubProcessingProvider extends hms.commons.KafkaProcessingProvider<KafkaHubProcessing>{
		@Inject
		public KafkaHubProcessingProvider(ApplicationLifecycle app, Injector injector) {
			super(app, injector);
		}

		@Override
		protected KafkaHubProcessing internalGet() {
			return new KafkaHubProcessing(
					this.injector.getInstance(Config.class),
					this.injector.getInstance(IHubServiceProcessor.class),
					this.injector.getInstance(hms.common.IHMSExecutorContext.class));
		}
	}	
	
	
	
	@Override
	protected void configure() {
        bind(hms.hub.repositories.IHubNodeRepository.class).to(hms.hub.repositories.HubNodeRepository.class);
        bind(hms.hub.IHubServiceProcessor.class).to(hms.hub.HubService.class);
        bind(hms.kafka.hub.KafkaHubProcessing.class)
        	.toProvider(KafkaHubProcessingProvider.class)
        	.asEagerSingleton();      
        
	}
}
