package hms.dto;
import java.util.UUID;

public class ProviderTracking implements LatLongLocation{
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
	
	public void setProviderid(UUID providerid) {
		this.providerid = providerid;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}	
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}		
}
