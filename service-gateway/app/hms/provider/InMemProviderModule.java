package hms.provider;

import com.google.inject.AbstractModule;

import hms.kafka.provider.InMemKafkaProviderSettings;
import hms.kafka.provider.KafkaProviderSettings;

public class InMemProviderModule  extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(KafkaProviderSettings.class).to(InMemKafkaProviderSettings.class);		
        bind(hms.provider.IProviderService.class)
        	.toProvider(KafkaProducerServiceProvider.class);        
	}
}
