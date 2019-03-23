package hms.provider.entities;

import java.util.UUID;

import org.bson.types.ObjectId;

import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Field;
import xyz.morphia.annotations.Id;
import xyz.morphia.annotations.Index;
import xyz.morphia.annotations.IndexOptions;
import xyz.morphia.annotations.Indexes;
import xyz.morphia.geo.Point;
import xyz.morphia.utils.IndexType;

@Indexes({
    @Index(fields = {@Field("providerid")}, options = @IndexOptions(unique = true,name = "tracking_indexing_providerid")),
    @Index(fields = {@Field(value="location", type = IndexType.GEO2DSPHERE)}, options = @IndexOptions(name = "tracking_indexing_location"))    
})
@Entity(value = "ProviderTracking")
public class ProviderTrackingEntity {
	@Id
	private ObjectId _id;	
    private UUID providerid;
    private UUID hubid;
    private Point location;
    
	public UUID getProviderid() {
		return providerid;
	}
	public void setProviderid(UUID providerid) {
		this.providerid = providerid;
	}
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
}
