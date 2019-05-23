package hms;

import java.util.UUID;

public class ProviderTrackingBuilder {
	private UUID providerid;
	private String name;
	private double latitude;
	private double longitude;
	private String zone;
	
	public ProviderTrackingBuilder(String zone) {
		this.zone = zone;
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
	public void setProviderid(UUID providerid) {
		this.providerid = providerid;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public hms.dto.Provider buildProvider(){
		return new hms.dto.Provider(this.providerid, this.zone, this.name);
	}
	public hms.dto.ProviderTracking buildTracking(){
		return new hms.dto.ProviderTracking(this.providerid, this.latitude, this.longitude);
	}
}
