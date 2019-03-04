package hms.dto;
import java.util.UUID;

public class ProviderTracking {
	private UUID providerid;
	private double latitude;
	private double longitude;	
	
	public ProviderTracking() {
		
	}
	
	public ProviderTracking(UUID providerid, double latitude, double longitude) {
		this.providerid = providerid;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public UUID getProviderid() {
		return providerid;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}	

	public ProviderTracking getSelf() {
		return this;
	}
}
