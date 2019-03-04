package hms.provider.entities;

import java.util.UUID;

import org.bson.types.ObjectId;

import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Field;
import xyz.morphia.annotations.Id;
import xyz.morphia.annotations.Index;
import xyz.morphia.annotations.IndexOptions;
import xyz.morphia.annotations.Indexes;
import xyz.morphia.annotations.NotSaved;
import xyz.morphia.annotations.Transient;

@Indexes({
    @Index(fields = {@Field("providerid")}, options = @IndexOptions(unique = true, name = "provider_indexing_providerid")),
})
@Entity(value = "Provider")
public class ProviderEntity {	
	@Id
	private ObjectId _id;
	private UUID providerid;
	private String name;
	private ProviderTrackingEntity currentTracking;
	@Transient
	@NotSaved
	private ProviderTrackingEntity previousTracking;
	
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

	public ProviderTrackingEntity getCurrentTracking() {
		return currentTracking;
	}	
	
	public ProviderTrackingEntity getPreviousTracking() {
		return this.previousTracking;
	}
	
	public void setCurrentTracking(ProviderTrackingEntity currentTracking) {
		if(this.previousTracking == null) {
			this.previousTracking = this.currentTracking;
		}
		this.currentTracking = currentTracking;
	}
}
