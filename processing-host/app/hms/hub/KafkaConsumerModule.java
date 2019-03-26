package hms.hub;

import com.google.inject.AbstractModule;

public class KafkaConsumerModule extends AbstractModule {
	@Override
	protected void configure() {
        bind(hms.hub.repositories.IHubNodeRepository.class).to(hms.hub.repositories.HubNodeRepository.class);
        bind(hms.hub.IHubServiceProcessor.class).to(hms.hub.HubService.class);
        bind(hms.kafka.hub.KafkaHubProcessing.class).asEagerSingleton();       
	}
}
