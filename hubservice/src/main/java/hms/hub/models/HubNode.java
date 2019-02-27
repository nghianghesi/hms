package hms.hub.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import xyz.morphia.annotations.Embedded;
import xyz.morphia.geo.GeoJson;
import xyz.morphia.geo.Point;

public class HubNode {
	private static final double DEFAULT_LATITUDE_RANGE = 90;
	private static final double DEFAULT_LONGITUDE_RANGE = 180;	
	
    private UUID hubid;
    private Point location;    
    private double latitudeRange;  // provider in hub when in range
    private double longitudeRange;  // provider in hub when in range
    private double margin = 0.5; // to reduce provider in/out hub. (out hub when out of range + margin) 
    @Embedded 
    private final List<HubNode> list = new ArrayList<HubNode>();
    
    public HubNode() { // default constructor for root hub
    	this.location = GeoJson.point(0, 0);
    	this.latitudeRange = DEFAULT_LATITUDE_RANGE;
    	this.longitudeRange = DEFAULT_LONGITUDE_RANGE;
    }
    
    public HubNode(HubNode parent, double latitude, double longitude, double rangeScale) { // constructor for subhub
    	this.location = GeoJson.point(latitude, longitude);
    	this.latitudeRange = Math.min(rangeScale, DEFAULT_LATITUDE_RANGE);
    	this.longitudeRange = Math.min(rangeScale * (DEFAULT_LONGITUDE_RANGE / DEFAULT_LATITUDE_RANGE), DEFAULT_LONGITUDE_RANGE);
    }   
    
    public HubNode(HubNode parent, double latitude, double longitude) { // constructor for subhub 
    	this(parent, latitude, longitude, 1);
    }
}
