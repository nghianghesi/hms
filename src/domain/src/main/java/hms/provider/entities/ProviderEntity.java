package hms.provider.entities;

import java.util.UUID;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.NotSaved;
import dev.morphia.annotations.Transient;
import dev.morphia.geo.Point;

@Indexes({
    @Index(fields = {@Field("providerid")}, options = @IndexOptions(unique = true, name = "provider_indexing_providerid")),
})
@Entity(value = "Provider")
public class ProviderEntity {	
	@Embedded
	public static class ProviderTrackingStruct{
	    private UUID hubid;
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
		private Point location;
	}
	@Id
	private ObjectId _id;
	private UUID providerid;
	private String name;
	private String zone;
	private ProviderTrackingStruct currentTracking;
	@Transient
	@NotSaved
	private ProviderTrackingStruct previousTracking;
	
	public UUID getProviderid() {
		return providerid;
	}
	
	public void setProviderid(UUID providerid) {
		this.providerid = providerid;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public ProviderTrackingStruct getCurrentTracking() {
		return currentTracking;
	}	
	
	public ProviderTrackingEntity getCurrentTrackingEntity() {
		if(this.currentTracking!=null) {
			ProviderTrackingEntity tracking =new ProviderTrackingEntity();
			tracking.setProviderid(this.providerid);
			tracking.setHubid(this.currentTracking.getHubid());
			tracking.setLocation(this.currentTracking.getLocation());
			return tracking;
		}
		return null;
	}
	
	public ProviderTrackingStruct getPreviousTracking() {
		return this.previousTracking;
	}
	
	public void setCurrentTracking(ProviderTrackingStruct currentTracking) {
		if(this.previousTracking == null) {
			this.previousTracking = this.currentTracking;
		}
		this.currentTracking = currentTracking;
	}
}
