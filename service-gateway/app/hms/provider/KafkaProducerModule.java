package hms.provider;

import com.google.inject.AbstractModule;

import hms.kafka.provider.KafkaProviderSettings;

public class KafkaProducerModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(KafkaProviderSettings.class).to(KafkaProviderSettings.class);
        bind(hms.provider.IProviderService.class)
        	.toProvider(KafkaProducerServiceProvider.class);        
	}
}
