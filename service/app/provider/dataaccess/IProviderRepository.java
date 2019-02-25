package provider.dataaccess;

import java.util.UUID;

import provider.models.ProviderTracking;

public interface IProviderRepository {

	ProviderTracking LoadById(UUID id);

	void Save(ProviderTracking tracking);
	
	void clear();

}