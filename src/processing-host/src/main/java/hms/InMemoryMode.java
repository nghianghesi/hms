package hms;

import org.springframework.context.support.GenericApplicationContext;

import dev.morphia.Datastore;
import hms.commons.HMSDbFactory;
import hms.kafka.provider.IProcessingService;
import hms.kafka.provider.InMemoryProviderProcessing;
import hms.provider.repositories.IProviderRepository;
import hms.provider.repositories.ProviderRepository;

public class InMemoryMode {
	public void registerBeans(GenericApplicationContext context) {

		context.registerBean(Datastore.class, () -> {
			return context.getBean(HMSDbFactory.class).get();
		});		
		context.registerBean(IProviderRepository.class, () -> new ProviderRepository());
		context.registerBean(IProcessingService.class, () -> new InMemoryProviderProcessing());
	}
}
