package hms;

import org.springframework.web.context.support.GenericWebApplicationContext;

import dev.morphia.Datastore;
import hms.commons.HMSDbFactory;
import hms.hub.HubService;
import hms.hub.IHubService;
import hms.hub.repositories.HubNodeRepository;
import hms.hub.repositories.IHubNodeRepository;

public class InMemMicroserviceMode {

	public void registerBeans(GenericWebApplicationContext context) {
		context.registerBean(HMSDbFactory.class, ()->new HMSDbFactory());
		context.registerBean(Datastore.class, () -> {
			return context.getBean(HMSDbFactory.class).get();
		});
		context.registerBean(IHubNodeRepository.class, () -> new HubNodeRepository());
		context.registerBean(IHubService.class, () -> new HubService());
	}
	
}
