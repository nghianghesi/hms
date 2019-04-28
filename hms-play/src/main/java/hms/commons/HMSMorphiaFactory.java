package hms.commons;

import javax.inject.Inject;


import it.unifi.cerm.playmorphia.PlayMorphia;
import xyz.morphia.Morphia;

public class HMSMorphiaFactory implements javax.inject.Provider<xyz.morphia.Morphia>{
	PlayMorphia playmorphia;
	@Inject
	public HMSMorphiaFactory(PlayMorphia playmorphia) {
		this.playmorphia = playmorphia;
	}
	@Override
	public Morphia get() {
		// TODO Auto-generated method stub
		return this.playmorphia.morphia();
	}

}
