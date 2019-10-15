package hms.provider.entities;

import java.util.UUID;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.geo.Point;
import dev.morphia.utils.IndexType;

@Indexes({
    @Index(fields = {@Field("providerid")}, options = @IndexOptions(name = "tracking_indexing_providerid")),
    @Index(fields = {
    		@Field("hubid"),
    		@Field(value="location", type = IndexType.GEO2DSPHERE)
    		}, options = @IndexOptions(name = "tracking_indexing_location")
    )    
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
