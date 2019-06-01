package hms;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ProviderTrackingBuilder {
	private UUID providerid;
	private String name;
	private double latitude;
	private double longitude;
	private String zone;
	private double latStep;
	private double longStep; 
	private int changePosibility=10;
	private double maxLat;
	private double maxLong;

	private double minLat;
	private double minLong;
	
	public ProviderTrackingBuilder(String zone, double latStep, double longStep, double minLat, double maxLat, double minLong, double maxLong) {
		this.zone = zone;
		this.latStep = latStep;
		this.longStep = longStep;
		this.minLat = minLat;
		this.minLong = minLong;

		this.maxLat = maxLat;
		this.maxLong = maxLong;
	}
	
	private static double normalizedBetween(double value, double minValue,double maxValue) {
		if(value<=minValue) {
			return minValue;
		}
		if(value>=maxValue) {
			return maxValue;
		}
		return value;
	}
	
	private static boolean isBetween(double value, double minValue,double maxValue) {
		return value >= minValue && value <= maxValue;
	}
	
	public void randomMove() {
		if(isBetween(this.latitude, this.minLat, this.maxLat)
			&&isBetween(this.longitude, this.minLong, this.maxLong)){
			if(ThreadLocalRandom.current().nextInt(this.changePosibility)<=1){
				this.latStep=-this.latStep;
			}
			if(ThreadLocalRandom.current().nextInt(this.changePosibility)<=1){
				this.longStep=-this.longStep;
			}			
		}else {
			if(ThreadLocalRandom.current().nextInt(2)<1){
				this.latStep=-this.latStep;
			}else {
				this.longStep=-this.longStep;
			}
		}
		
		this.latitude=normalizedBetween(this.latitude+this.latStep, this.minLat, this.maxLat);
		this.longitude=normalizedBetween(this.longitude+this.longStep,this.minLong, this.maxLong);		
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
