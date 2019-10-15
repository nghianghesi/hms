package hms.dto;

import java.util.UUID;

public class HubProviderTracking implements LatLongLocation{
	private UUID hubid;
	private UUID providerid;
	private double latitude;
	private double longitude;	
	
	public HubProviderTracking() {
		
	}
	
	public HubProviderTracking(UUID hubid, UUID providerid, double latitude, double longitude) {
		this.hubid = hubid;
		this.providerid = providerid;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public UUID getHubid() {
		return hubid;
	}

	public void setHubid(UUID hubid) {
		this.hubid = hubid;
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
