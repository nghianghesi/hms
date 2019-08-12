package hms.dto;

public class GeoQuery implements LatLongLocation{
	private double latitude;
	private double longitude;	
	private int distance;	
	
	public GeoQuery() {
		
	}
	
	public GeoQuery(double latitude, double longitude, int distance) {
		this.distance = distance;
		this.latitude = latitude;
		this.longitude = longitude;
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
