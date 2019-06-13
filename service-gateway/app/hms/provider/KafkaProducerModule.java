package hms.provider;

import com.google.inject.AbstractModule;

import hms.kafka.provider.KafkaProviderSettings;
import hms.kafka.provider.KafkaProviderTopics;

public class KafkaProducerModule extends AbstractModule {
	@Override
	protected void configure() {

		bind(KafkaProviderTopics.class).to(KafkaProviderSettings.class);			
        bind(hms.provider.IAsynProviderService.class)
        	.toProvider(KafkaProviderServiceFactory.class).asEagerSingleton();        
	}
}
