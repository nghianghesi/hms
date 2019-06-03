package hms.hub.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import xyz.morphia.annotations.Embedded;
import xyz.morphia.geo.Point;

@Embedded
public class HubSubEntity implements HubNodeEntity{
    private UUID hubid;
    private String name;
    private Point location;   

	private double latitudeRange;  // provider in hub when in range
    private double longitudeRange;  // provider in hub when in range
    private double margin; // to reduce number of in/out hub. (moving out of hub when out of range + margin)
    
    @Embedded("SubHubs")
    private final List<HubSubEntity> subHubs = new ArrayList<HubSubEntity>();
    
	public UUID getHubid() {
		return hubid;
	}
	public void setHubid(UUID hubid) {
		this.hubid = hubid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Point getLocation() {
		return location;
	}
	public void setLocation(Point location) {
		this.location = location;
	}
	public double getLatitudeRange() {
		return latitudeRange;
	}
	public void setLatitudeRange(double latitudeRange) {
		this.latitudeRange = latitudeRange;
	}
	public double getLongitudeRange() {
		return longitudeRange;
	}
	public void setLongitudeRange(double longitudeRange) {
		this.longitudeRange = longitudeRange;
	}
	public double getMargin() {
		return margin;
	}
	public void setMargin(double margin) {
		this.margin = margin;
	}
	public List<HubSubEntity> getSubHubs() {
		return subHubs;
	}		
	public void setSubHubs(List<HubSubEntity> val) {
		this.subHubs.clear();
		if(val!=null) {
			this.subHubs.addAll(val);
		}
	}
}
