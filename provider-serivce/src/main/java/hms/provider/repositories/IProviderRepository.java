package hms.provider.repositories;

import java.util.UUID;

import hms.provider.models.ProviderModel;

public interface IProviderRepository {

	ProviderModel LoadById(UUID id);

	void Save(hms.provider.models.ProviderModel provider);
	
	void SaveTracking(hms.provider.models.ProviderModel provider);
	
	void clear();

}