package hms;

import org.springframework.web.context.support.GenericWebApplicationContext;

import dev.morphia.Datastore;
import dev.morphia.Morphia;
import hms.commons.HMSDbFactory;
import hms.hub.HubService;
import hms.hub.IHubService;
import hms.hub.IKubernetesHub;
import hms.hub.KubernetesHub;
import hms.hub.repositories.HubNodeRepository;
import hms.hub.repositories.IHubNodeRepository;
import hms.kafka.provider.InMemKafkaProviderSettings;
import hms.kafka.provider.KafkaProviderTopics;
import hms.provider.IAsynProviderService;
import hms.provider.IProviderInitializingService;
import hms.provider.KafkaHubProviderService;
import hms.provider.ProviderInitializer;

public class InMemMicroserviceMode {

	public void registerBeans(GenericWebApplicationContext context) {
		context.registerBean(HMSDbFactory.class, ()->new HMSDbFactory());
		context.registerBean(Datastore.class, () -> {
			return context.getBean(HMSDbFactory.class).getDatastore();
		});
		context.registerBean(Morphia.class, () -> {
			return context.getBean(HMSDbFactory.class).getMorfia();
		});
		context.registerBean(IHubNodeRepository.class, () -> new HubNodeRepository());
		context.registerBean(IHubService.class, () -> new HubService());
		context.registerBean(KafkaProviderTopics.class, () -> new InMemKafkaProviderSettings());
		context.registerBean(KafkaHubProviderService.class, () -> new KafkaHubProviderService());
		//context.registerBean(IAsynProviderService.class, () -> new KafkaHubProviderService());
		context.registerBean(IProviderInitializingService.class, ()-> new ProviderInitializer());
		context.registerBean(IKubernetesHub.class, ()-> new KubernetesHub());
	}
	
}
