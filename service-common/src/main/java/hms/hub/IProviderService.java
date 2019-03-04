package hms.hub;

import java.security.InvalidKeyException;

public interface IProviderService {
	void clear();
	void initprovider(hms.dto.Provider providerdto);
	void tracking(hms.dto.ProviderTracking trackingdto) throws InvalidKeyException;
}
