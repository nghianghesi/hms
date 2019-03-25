package hms.commons;

import com.google.inject.AbstractModule;


public class HMSModule extends AbstractModule {
	@Override
	protected void configure() {
		//bind(HMSDbFactory.class).to(HMSDbFactory.class);
        bind(xyz.morphia.Datastore.class).toProvider(HMSDbFactory.class);
	}
}