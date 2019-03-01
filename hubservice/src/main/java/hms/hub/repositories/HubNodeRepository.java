package hms.hub.repositories;

import javax.inject.Inject;
import HMSPlayMorphia;
import hms.hub.HubNodePlayMorphia;
import it.unifi.cerm.playmorphia.PlayMorphia;

public class HubNodeRepository implements IHubNodeRepository {
	private PlayMorphia morphia;
	@Inject
	public HubNodeRepository(HubNodePlayMorphia morphia) {
		this.morphia = morphia;
	}	
}
