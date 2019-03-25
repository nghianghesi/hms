package hms.commons;

import javax.inject.Inject;

import it.unifi.cerm.playmorphia.PlayMorphia;
import xyz.morphia.Datastore;

public class HMSDbFactory implements javax.inject.Provider<xyz.morphia.Datastore>{
	PlayMorphia playmorphia;
	@Inject
	public HMSDbFactory(PlayMorphia playmorphia) {
		this.playmorphia = playmorphia;
	}
	@Override
	public Datastore get() {
		return playmorphia.datastore();
	}

}
