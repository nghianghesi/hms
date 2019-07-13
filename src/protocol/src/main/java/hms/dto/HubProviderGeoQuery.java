package hms.dto;

import java.util.UUID;

public class HubProviderGeoQuery implements LatLongLocation{
	private UUID hubid;
	private double latitude;
	private double longitude;	
	private int distance;	
	
	public HubProviderGeoQuery() {
		
	}
	
	public HubProviderGeoQuery(UUID hubid, double latitude, double longitude, int distance) {
		this.hubid = hubid;
		this.distance = distance;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public UUID getHubid() {
		return hubid;
	}

	public void setHubid(UUID hubid) {
		this.hubid = hubid;
	}
	
	public int getDistance() {
		return distance;
	}	
	
	public void setDistance(int distance) {
		this.distance = distance;
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
