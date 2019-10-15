package hms.hub;

import java.util.List;
import java.util.UUID;

public interface IHubServiceProcessor {
	UUID getHostingHubId(double latitude, double longitude);
	List<UUID> getConveringHubs(hms.dto.GeoQuery query);	
}
