package hms.provider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import hms.kafka.provider.InMemoryProviderProcessing;
import hms.kafka.provider.KafkaProviderProcessing;
import play.inject.ApplicationLifecycle;

public class KafkaConsumerModule extends AbstractModule {
	
	public static class KafkaProviderProcessingProvider extends hms.commons.KafkaProcessingProvider<KafkaProviderProcessing> {

		@Inject
		public KafkaProviderProcessingProvider(ApplicationLifecycle app, Injector injector) {
			super(app, injector);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected KafkaProviderProcessing internalGet() {
			return new KafkaProviderProcessing(
					this.injector.getInstance(Config.class),
					this.injector.getInstance(IProviderServiceProcessor.class),
					this.injector.getInstance(hms.common.IHMSExecutorContext.class));
		}
	}	
	
	private static class InMemProviderProcessingProvider 
		extends hms.commons.KafkaProcessingProvider<InMemoryProviderProcessing> {

		@Inject
		public InMemProviderProcessingProvider(ApplicationLifecycle app, Injector injector) {
			super(app, injector);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected InMemoryProviderProcessing internalGet() {
			return new InMemoryProviderProcessing(
					this.injector.getInstance(Config.class),
					this.injector.getInstance(hms.common.IHMSExecutorContext.class));
		}

	}		
	
	@Override
	protected void configure() {
        bind(hms.provider.repositories.IProviderRepository.class).to(hms.provider.repositories.ProviderRepository.class);
        bind(hms.provider.IProviderServiceProcessor.class).to(hms.provider.ProviderServiceProcessor.class);
        bind(hms.kafka.provider.KafkaProviderProcessing.class)
        	.toProvider(KafkaProviderProcessingProvider.class)
        	.asEagerSingleton();
        
        bind(InMemoryProviderProcessing.class)
        	.toProvider(InMemProviderProcessingProvider.class)
        	.asEagerSingleton();  
	}
}
