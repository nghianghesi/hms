package hms.provider;

import javax.inject.Inject;

import it.unifi.cerm.playmorphia.PlayMorphia;

import com.typesafe.config.Config;
import play.Environment;
import play.inject.ApplicationLifecycle;
import xyz.morphia.mapping.DefaultCreator;

import javax.inject.Singleton;

@Singleton
public class ProviderPlayMorphia extends PlayMorphia{
	@Inject
    public ProviderPlayMorphia(ApplicationLifecycle lifecycle, Environment env, Config config) {
		super(lifecycle, env, config);

		// Configuring class loader.
        this.morphia().getMapper().getOptions().setObjectFactory(new DefaultCreator() {
            @Override
            protected ClassLoader getClassLoaderForClass() {
                return env.classLoader();
            }
        });
        //this.morphia().map(hms.provider.entities.ProviderEntity.class);        
        //this.datastore().ensureIndexes();    
        //this.morphia().map(hms.provider.entities.ProviderTrackingEntity.class);        
        //this.datastore().ensureIndexes();    
	}
}
