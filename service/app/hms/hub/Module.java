package hms.hub;

import com.google.inject.AbstractModule;

public class Module extends AbstractModule {
	@Override
	protected void configure() {
        bind(hms.hub.repositories.IHubNodeRepository.class).to(hms.hub.repositories.HubNodeRepository.class).asEagerSingleton();        
        //bind(hms.hub.HubNodePlayMorphia.class).to(hms.hub.HubNodePlayMorphia.class).asEagerSingleton();
        bind(hms.hub.IHubService.class).to(hms.hub.HubService.class).asEagerSingleton();        
	}
}
