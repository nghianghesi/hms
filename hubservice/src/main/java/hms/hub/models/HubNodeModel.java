package hms.hub.models;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

import hms.hub.entities.HubNodeEntity;
import hms.hub.entities.HubRootEntity;
import hms.hub.entities.HubSubEntity;
import xyz.morphia.geo.GeoJson;
import xyz.morphia.geo.Point;

public class HubNodeModel {
	private static final double DEFAULT_LATITUDE_RANGE = 90;
	private static final double DEFAULT_LONGITUDE_RANGE = 180;	
	
	private HubNodeEntity entity;
	private List<HubNodeModel> subHubs = new ArrayList<HubNodeModel>();
    
    public HubNodeModel() { // default constructor for root hub
    	this.entity = new HubRootEntity();
    	this.entity.setHubid(UUID.randomUUID());
    	this.entity.setLocation(GeoJson.point(0, 0));
    	this.entity.setLatitudeRange(DEFAULT_LATITUDE_RANGE);
    	this.entity.setLongitudeRange(DEFAULT_LONGITUDE_RANGE);
    }
    
    public HubNodeModel(HubNodeModel parent, double latitude, double longitude, double rangeScale) { // constructor for subhub
    	this();
    	if(parent!=null) {
    		HubSubEntity sub = new HubSubEntity();
    		this.entity = sub;
        	this.entity.setHubid(UUID.randomUUID());
	    	this.entity.setLocation(GeoJson.point(latitude, longitude));
	    	this.entity.setLatitudeRange(Math.min(rangeScale, DEFAULT_LATITUDE_RANGE));
	    	this.entity.setLongitudeRange(Math.min(rangeScale * (DEFAULT_LONGITUDE_RANGE / DEFAULT_LATITUDE_RANGE), DEFAULT_LONGITUDE_RANGE));   	
	    	parent.subHubs.add(this);
	    	parent.entity.getSubHubs().add(sub);
    	}
    }
    
    public HubNodeModel(HubNodeEntity entity) {
    	this.entity = entity;
    	for(HubNodeEntity e : entity.getSubHubs()) {
    		HubNodeModel sub = new HubNodeModel(e);
    		this.subHubs.add(sub);
    	}
    }   
    
    public HubNodeEntity persistance() {
    	return this.entity;
    }
    
    public UUID getHubid() {
		return this.entity.getHubid();
	}

	public void setHubid(UUID hubid) {
		this.entity.setHubid(hubid);
	}

	public Point getLocation() {
		return this.entity.getLocation();
	}

	public void setLocation(Point location) {
		this.entity.setLocation(location);
	}

	public double getLatitudeRange() {
		return this.entity.getLatitudeRange();
	}

	public void setLatitudeRange(double latitudeRange) {
		this.entity.setLatitudeRange(latitudeRange);
	}

	public double getLongitudeRange() {
		return this.entity.getLongitudeRange();
	}

	public void setLongitudeRange(double longitudeRange) {
		this.entity.setLongitudeRange(longitudeRange);
	}

	public double getMargin() {
		return this.entity.getMargin();
	}

	public void setMargin(double margin) {
		this.entity.setMargin(margin);
	}    
	
	public boolean isRangeContains(double latitude, double longitude) {
		return this.getLocation().getLatitude() - this.getLatitudeRange() >= latitude
				 && this.getLocation().getLatitude() + this.getLatitudeRange() <= latitude
				 && this.getLocation().getLongitude() - this.getLongitudeRange() >= longitude
						 && this.getLocation().getLongitude() + this.getLongitudeRange() <= longitude;
	}	
	
	public boolean isRangeMovedOut(double latitude, double longitude) {
		return !(this.getLocation().getLatitude() - this.getLatitudeRange() - this.getMargin()>= latitude
				 && this.getLocation().getLatitude() + this.getLatitudeRange() + this.getMargin()<= latitude
				 && this.getLocation().getLongitude() - this.getLongitudeRange() - this.getMargin()>= longitude
						 && this.getLocation().getLongitude() + this.getLongitudeRange() + this.getMargin() <= longitude);
	}
	
	private double rangeDistance(double latitude, double longitude) {
		return Math.sqrt(latitude - this.getLocation().getLatitude())
				+ Math.sqrt(longitude - this.getLocation().getLongitude());
	}
	
	public HubNodeModel getHostingHub(double latitude, double longitude) {
		double minRange = -1, temptRangeDistance;
		HubNodeModel res = null;
		for(HubNodeModel node : this.subHubs) {
			if(node.isRangeContains(latitude, longitude)) {
				temptRangeDistance = node.rangeDistance(latitude, longitude);
				if(minRange == -1 || minRange > temptRangeDistance) {
					minRange = temptRangeDistance;
					res = node;
				}
			}
		}
		return res != null ? res.getHostingHub(latitude, longitude) : this;
	}
	
	public String getDebugInfo() {
		StringJoiner str = new StringJoiner(",");
		str.add(this.entity.getHubid().toString());
		str.add(""+this.getLocation().getLongitude());
		str.add(""+this.getLocation().getLatitude());
		if(this.subHubs.size()>0) {
			str.add("[");
			for(HubNodeModel node : this.subHubs) {
				str.add(node.getDebugInfo());
			}
			str.add("]");
		}
		return str.toString();
	}
}
