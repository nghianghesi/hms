package hms.provider;

import com.google.inject.AbstractModule;

import hms.hub.HubService;
import hms.hub.IHubService;
import hms.hub.repositories.HubNodeRepository;
import hms.hub.repositories.IHubNodeRepository;
import hms.kafka.provider.InMemKafkaProviderSettings;
import hms.kafka.provider.KafkaProviderSettings;

public class InMemProviderModule  extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(IHubNodeRepository.class).to(HubNodeRepository.class);
		bind(IHubService.class).to(HubService.class);
		bind(KafkaProviderSettings.class).to(InMemKafkaProviderSettings.class);		
        bind(hms.provider.IAsynProviderService.class)
        	.toProvider(KafkaProducerServiceProvider.class).asEagerSingleton();;        
	}
}
