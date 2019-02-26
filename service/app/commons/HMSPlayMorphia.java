package commons;

import javax.inject.Inject;

import it.unifi.cerm.playmorphia.PlayMorphia;


import com.typesafe.config.Config;
import play.Environment;
import play.inject.ApplicationLifecycle;

import javax.inject.Singleton;

/**
 * Created by morelli on 21/02/19.
 */
@Singleton
public class HMSPlayMorphia extends PlayMorphia{
	@Inject
    public HMSPlayMorphia(ApplicationLifecycle lifecycle, Environment env, Config config) {
		super(lifecycle, env, config);

        this.morphia().map(provider.models.ProviderTracking.class);        
        this.datastore().ensureIndexes();    
	}
}
