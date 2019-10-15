package hms.dto;

public class Coordinate implements LatLongLocation{
	private double latitude;
	private double longitude;
	public Coordinate() {
	}
	public Coordinate(double latitude, double longitude) {		
		this.latitude = latitude;
		this.longitude = longitude;
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
