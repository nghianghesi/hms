package hms.hub;

import javax.inject.Inject;

import com.typesafe.config.Config;

import it.unifi.cerm.playmorphia.PlayMorphia;
import play.Environment;
import play.inject.ApplicationLifecycle;

public class HubNodePlayMorphia extends PlayMorphia{
	@Inject
    public HubNodePlayMorphia(ApplicationLifecycle lifecycle, Environment env, Config config) {
		super(lifecycle, env, config);

        this.morphia().map(hms.hub.entities.HubNodeEntity.class);        
        this.datastore().ensureIndexes();    
	}
}
