package hms.commons;

import com.google.inject.AbstractModule;

public class HMSDbModule extends AbstractModule {
	@Override
	protected void configure() {
        bind(xyz.morphia.Datastore.class).toProvider(HMSDbFactory.class);
        bind(xyz.morphia.Morphia.class).toProvider(HMSMorphiaFactory.class);
        
	}
}