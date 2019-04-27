package hms.dto;

public class GeoQuery {
	private double latitude;
	private double longitude;	
	private double distance;	
	
	public GeoQuery() {
		
	}
	
	public GeoQuery(double latitude, double longitude, double distance) {
		this.distance = distance;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public double getDistance() {
		return distance;
	}	
	
	public void setDistance(double distance) {
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
