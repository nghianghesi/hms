package hms;

import org.springframework.context.support.GenericApplicationContext;

import dev.morphia.Datastore;
import dev.morphia.Morphia;
import hms.commons.HMSDbFactory;
import hms.provider.InMemoryProviderProcessing;
import hms.provider.repositories.IProviderRepository;
import hms.provider.repositories.ProviderRepository;

public class InMemoryMode {
	public void registerBeans(GenericApplicationContext context) {
		context.registerBean(HMSDbFactory.class, () -> new HMSDbFactory());		
		
		context.registerBean(Morphia.class, () -> {
			return context.getBean(HMSDbFactory.class).getMorfia();
		});	
		context.registerBean(Datastore.class, () -> {
			return context.getBean(HMSDbFactory.class).getDatastore();
		});		
		context.registerBean(IProviderRepository.class, () -> new ProviderRepository());
		context.registerBean(IProcessingService.class, () -> new InMemoryProviderProcessing());
	}
}
