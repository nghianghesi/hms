package hms;

import hms.dto.Coordinate;

public class ProviderQueryBuilder {
	private double latitude;
	private double longitude;
	
	
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
	public Coordinate build(){
		return new Coordinate(this.latitude, this.longitude);
	}
}
