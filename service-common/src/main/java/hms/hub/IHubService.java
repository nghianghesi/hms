package hms.hub;

import java.util.UUID;

public interface IHubService {

	UUID getHostingHubId(double latitude, double longitude);

}