package hms.hub.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import hms.common.DistanceUtils;
import hms.hub.entities.HubNodeEntity;
import hms.hub.entities.HubRootEntity;
import hms.hub.entities.HubSubEntity;
import dev.morphia.geo.GeoJson;
import dev.morphia.geo.Point;

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
	
    public String getName() {
		return this.entity.getName();
	}

	public void setName(String name) {
		this.entity.setName(name);
	}	
    public String getZone() {
		return this.entity.getZone();
	}

	public void setZone(String zone) {
		this.entity.setZone(zone);
	}	
	public double getLatitude() {
		return this.entity.getLocation().getLatitude();
	}
	
	public double getLongitude() {
		return this.entity.getLocation().getLongitude();
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
	
	public boolean getIsActive() {
		return this.entity.getIsActive();
	}

	public void setIsActive(boolean isActive) {
		this.entity.setIsActive(isActive);
	}    
	
	public List<HubNodeModel> getSubHubs(){
		return this.subHubs;
	}
	
	private double inRangeDistance = -1;
	private double getInRangeDistance() {
		if(this.inRangeDistance < 0) {
			return Math.max(this.geoDistance(this.getLatitude() - this.getLatitudeRange(), 
												this.getLongitude() - this.getLongitudeRange()),
					Math.max(this.geoDistance(this.getLatitude() - this.getLatitudeRange(), 
												this.getLongitude() + this.getLongitudeRange()),
					Math.max(this.geoDistance(this.getLatitude() + this.getLatitudeRange(), 
													this.getLongitude() - this.getLongitudeRange()),
							 this.geoDistance(this.getLatitude() + this.getLatitudeRange() , 
														this.getLongitude() + this.getLongitudeRange()))));
		}else {
			return this.inRangeDistance;
		}
	}
	
	private double inMarginDistance = -1;
	private double getInMarginDistance() {
		if(this.inMarginDistance < 0) {
			return Math.max(this.geoDistance(this.getLatitude() - this.getLatitudeRange() - this.getMargin(), 
												this.getLongitude() - this.getLongitudeRange() - this.getMargin() ),
					Math.max(this.geoDistance(this.getLatitude() - this.getLatitudeRange() - this.getMargin(), 
												this.getLongitude() + this.getLongitudeRange() + this.getMargin()),
					Math.max(this.geoDistance(this.getLatitude() + this.getLatitudeRange() + this.getMargin(), 
													this.getLongitude() - this.getLongitudeRange() - this.getMargin()),
								this.geoDistance(this.getLatitude() + this.getLatitudeRange() + this.getMargin(), 
														this.getLongitude() + this.getLongitudeRange() + this.getMargin()))));
		}else {
			return this.inMarginDistance;
		}
	}	
	
	public boolean isRangeContains(double latitude, double longitude) {
		return this.getLatitude() - this.getLatitudeRange() <= latitude
				 && this.getLatitude() + this.getLatitudeRange() >= latitude
				 && this.getLongitude() - this.getLongitudeRange() <= longitude
						 && this.getLongitude() + this.getLongitudeRange() >= longitude;
	}	
	
	public boolean isRangeMovedOut(double latitude, double longitude) {
		return !(this.getLatitude() - this.getLatitudeRange() - this.getMargin() <= latitude
				 && this.getLatitude() + this.getLatitudeRange() + this.getMargin() >= latitude
				 && this.getLongitude() - this.getLongitudeRange() - this.getMargin() <= longitude
						 && this.getLongitude() + this.getLongitudeRange() + this.getMargin() >= longitude);
	}
	
	private double rangeEstimation(double latitude, double longitude) {
		return Math.sqrt(latitude - this.getLatitude()) + Math.sqrt(longitude - this.getLongitude());
	}
	
	private double geoDistance(double lat, double lng) {
	    return DistanceUtils.geoDistance(lat, lng, this.getLatitude(), this.getLongitude());
	}	
	
	public HubNodeModel getHostingHub(double latitude, double longitude) {
		double minRange = -1.0, temptRangeDistance;
		HubNodeModel res = null;
		for(HubNodeModel node : this.subHubs) {
			if(node.isRangeContains(latitude, longitude)) {
				temptRangeDistance = node.rangeEstimation(latitude, longitude);
				if(minRange < 0 || minRange > temptRangeDistance) {
					minRange = temptRangeDistance;
					res = node;
				}
			}
		}
		return res != null ? res.getHostingHub(latitude, longitude) : this;
	}
	
	public List<HubNodeModel> getConveringHubIds(double latitude, double longitude, double distance){
		List<HubNodeModel> res = new ArrayList<HubNodeModel>();
		boolean needParent = true;
		for(HubNodeModel node : this.subHubs) {
			double nodeGeoDistance = node.geoDistance(latitude, longitude);
			needParent = needParent && (nodeGeoDistance + distance > node.getInRangeDistance());
			if((nodeGeoDistance - distance < node.getInMarginDistance())) {
				res.addAll(node.getConveringHubIds(latitude, longitude, distance));
			}
		}
		if(needParent) {
			res.add(this);
		}
		return res;
	}
	
	public void split(UUID id, int parts) {
		if(this.getHubid().equals(id)) {
			if(this.subHubs==null || this.subHubs.size() == 0) {
				this.entity.getSubHubs().clear();
				this.subHubs = new ArrayList<HubNodeModel>();
				double latDiff = this.getLatitudeRange()/parts;
				double longDiff = this.getLongitudeRange()/parts;
				
				double subLat=this.getLatitude()-this.getLatitudeRange();
				double subLong=this.getLongitude()-this.getLongitudeRange();
				
				double subLatrange = this.getLatitudeRange() / parts;
				double subLongrange = this.getLongitudeRange() / parts;
				for(int i=0;i<parts;i++) {
					subLat += latDiff * 2;
					subLong=this.getLongitude()-this.getLongitudeRange();
					for(int j=0;j<parts;j++) {
						subLong+=longDiff*2;
						HubSubEntity sub = new HubSubEntity();
						sub.setHubid(UUID.randomUUID());
						sub.setName(this.getName()+i+j);
						sub.setLocation(GeoJson.point(subLat,subLong));
						sub.setLatitudeRange(subLatrange);
						sub.setLongitudeRange(subLongrange);
						sub.setMargin(this.getMargin());
						this.entity.getSubHubs().add(sub);
						this.subHubs.add(new HubNodeModel(sub));
					}
				}
			}
		}else {
			if(this.subHubs!=null) {
				for(HubNodeModel sub : this.subHubs) {
					sub.split(id, parts);
				}
			}
		}
	}
	
	public void collectNodes(Map<UUID,HubNodeModel> res) {
		res.put(this.getHubid(), this);
		this.subHubs.forEach((sub)->sub.collectNodes(res));
	}
	
	public String getDebugInfo() {
		StringJoiner str = new StringJoiner(",");
		str.add(this.entity.getHubid().toString());
		str.add(""+this.getLongitude());
		str.add(""+this.getLatitude());
		str.add(""+this.getLatitudeRange());
		str.add(""+this.getLongitudeRange());
		str.add(""+this.getMargin());
		str.add(this.getName());
		str.add(this.getZone());
		str.add("\n");
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
