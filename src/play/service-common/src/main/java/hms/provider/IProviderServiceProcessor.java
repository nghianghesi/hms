package hms.provider;

import java.util.List;
import java.util.UUID;

public interface IProviderServiceProcessor{		
	Boolean tracking(hms.dto.ProviderTracking trackingdto, UUID hubid);
	List<hms.dto.Provider> queryProviders(List<UUID>hostids, hms.dto.GeoQuery query);
}
