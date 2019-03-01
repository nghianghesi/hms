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
    @Index(fields = {@Field("providerid")}, options = @IndexOptions(unique = true,name = "indexing_providerid")),
    @Index(fields = {@Field(value="location", type = IndexType.GEO2DSPHERE)}, options = @IndexOptions(name = "indexing_location"))    
})
@Entity(value = "ProviderTracking")
public class ProviderTrackingEntity {
	@Id
    private ObjectId id;
    private UUID providerid;
    private UUID hubid;
    private Point location;
}
