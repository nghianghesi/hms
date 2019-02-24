package provider.dataaccess;

import java.util.UUID;
import javax.inject.Inject;
import it.unifi.cerm.playmorphia.PlayMorphia;
import provider.models.ProviderTracking;

public class ProviderRepository implements IProviderRepository {
	
	private PlayMorphia morphia;
	@Inject
	public ProviderRepository(PlayMorphia morphia) {
		this.morphia=morphia;
	}	

	@Override
	public ProviderTracking LoadById(UUID id){
		if(id!=null) {
			return this.InternalLoadById(id);
		}else {
			return null;
		}
	}
	
	@Override
	public void Save(ProviderTracking tracking) {
        morphia.datastore().save(tracking);        
	}
	
	private provider.models.ProviderTracking InternalLoadById(UUID id){
		return this.morphia.datastore()
					.createQuery(ProviderTracking.class).field("id").equal(id)
					.get();
		
	}
}
