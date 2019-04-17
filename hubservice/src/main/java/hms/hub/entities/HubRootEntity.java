package hms.hub.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bson.types.ObjectId;

import xyz.morphia.annotations.Embedded;
import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Field;
import xyz.morphia.annotations.Id;
import xyz.morphia.annotations.Index;
import xyz.morphia.annotations.IndexOptions;
import xyz.morphia.annotations.Indexes;
import xyz.morphia.geo.Point;
import xyz.morphia.utils.IndexType;


@Indexes({
    @Index(fields = {@Field("hubid")}, options = @IndexOptions(unique = true,name = "hub_indexing_hubid")),
    @Index(fields = {@Field(value="location", type = IndexType.GEO2DSPHERE)}, options = @IndexOptions(name = "hub_indexing_location"))    
})
@Entity(value = "HubNode")
public class HubRootEntity implements HubNodeEntity{
	@Id
	private ObjectId _id;
    private UUID hubid;
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
