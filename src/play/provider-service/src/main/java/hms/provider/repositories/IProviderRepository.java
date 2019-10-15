package hms.provider.repositories;

import java.util.List;
import java.util.UUID;

import hms.provider.models.ProviderModel;

public interface IProviderRepository {

	ProviderModel LoadById(UUID id);

	void Save(hms.provider.models.ProviderModel provider);
	
	void SaveTracking(hms.provider.models.ProviderModel provider);
	
	void clear();
	
	void clearByZone(String name);
	
	List<hms.provider.models.ProviderModel> geoSearchProviders(List<UUID> hostid, double latitude, double longitude, int distance);
	
	List<hms.provider.models.ProviderModel> getProvidersByIds(List<UUID> providerids);
	
	List<hms.provider.models.ProviderModel> getProvidersByZone(String zone);
}