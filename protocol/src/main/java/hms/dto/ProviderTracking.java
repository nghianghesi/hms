package hms.dto;
import java.util.UUID;

public class ProviderTracking {
	public UUID id;
	public double latitude;
	public double longitude;	
	public ProviderTracking getSelf() {
		return this;
	}
}
