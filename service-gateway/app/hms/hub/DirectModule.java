package hms.hub;

import com.google.inject.AbstractModule;
public class DirectModule extends AbstractModule {
	@Override
	protected void configure() {
        bind(hms.hub.repositories.IHubNodeRepository.class).to(hms.hub.repositories.HubNodeRepository.class);        
        bind(hms.hub.IHubService.class).to(hms.hub.HubService.class);
	}
}
