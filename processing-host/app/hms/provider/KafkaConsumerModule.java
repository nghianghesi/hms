package hms.provider;

import com.google.inject.AbstractModule;

public class KafkaConsumerModule extends AbstractModule {
	@Override
	protected void configure() {
        bind(hms.provider.repositories.IProviderRepository.class).to(hms.provider.repositories.ProviderRepository.class);
        bind(hms.provider.IProviderServiceProcessor.class).to(hms.provider.ProviderServiceProcessor.class);
        bind(hms.kafka.provider.KafkaProviderProcessing.class)
        	.toProvider(hms.provider.KafkaProviderProcessingProvider.class)
        	.asEagerSingleton();       
	}
}
