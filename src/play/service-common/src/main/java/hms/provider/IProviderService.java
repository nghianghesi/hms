package hms.provider;

import java.util.List;

public interface IProviderService extends IAsynProviderService {
	Boolean tracking(hms.dto.ProviderTracking trackingdto);
	List<hms.dto.Provider> queryProviders(hms.dto.GeoQuery query);	
}

