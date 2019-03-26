package hms.commons;

import javax.inject.Inject;
import com.typesafe.config.Config;
import it.unifi.cerm.playmorphia.PlayMorphia;
import play.Environment;
import xyz.morphia.Datastore;
import xyz.morphia.mapping.DefaultCreator;

public class HMSDbFactory implements javax.inject.Provider<xyz.morphia.Datastore>{
	PlayMorphia playmorphia;
	@Inject
	public HMSDbFactory(PlayMorphia playmorphia,Config config, Environment env) {
		this.playmorphia = playmorphia;
		
		// Configuring class loader.
		this.playmorphia.morphia().getMapper().getOptions().setObjectFactory(new DefaultCreator() {
            @Override
            protected ClassLoader getClassLoaderForClass() {
                return env.classLoader();
            }
        });
		
		String models = config.getString("playmorphia.models");
		if(models!=null && models.compareTo("")>0) {
			for(String s : models.split(",")) {
				this.playmorphia.morphia().mapPackage(s);
			}
			this.playmorphia.datastore().ensureIndexes();
			this.playmorphia.datastore().ensureCaps();					
		}			
	}
	
	@Override
	public Datastore get() {
		return playmorphia.datastore();
	}

}
