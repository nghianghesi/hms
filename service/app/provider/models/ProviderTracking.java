package provider.models;

import java.util.UUID;

import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Field;
import xyz.morphia.annotations.Id;
import xyz.morphia.annotations.Index;
import xyz.morphia.annotations.Indexes;
import xyz.morphia.geo.Point;
import xyz.morphia.utils.IndexType;
import xyz.morphia.annotations.IndexOptions;

@Indexes({
    @Index(fields = {@Field("providerid")}, options = @IndexOptions(unique = true,name = "indexing_providerid")),
    @Index(fields = {@Field(value="location", type = IndexType.GEO2DSPHERE)}, options = @IndexOptions(name = "indexing_location"))    
})
@Entity(value = "ProviderTracking")
public class ProviderTracking{
	@Id
    private ObjectId id;
    private UUID providerid;
    private Point location;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	
	public UUID getProviderid() {
		return providerid;
	}
	public void setProviderid(UUID id) {
		this.providerid = id;
	}
	
	public Point getLocation() {
		return location;
	}
	
	public void setLocation(Point location) {
		this.location = location;
	}		
	
	public void setLocation(double lat, double lon) {
		this.location = xyz.morphia.geo.GeoJson.point(lat, lon);
	}		
	
	public void setLocation(hms.dto.ProviderTracking dto) {
		this.location = xyz.morphia.geo.GeoJson.point(dto.latitude, dto.longitude);
	}	

	private static final ModelMapper modelMapper = new ModelMapper();
	static {
		modelMapper.addMappings(new PropertyMap<hms.dto.ProviderTracking, ProviderTracking>() {			
			@Override
			protected void configure() {
				map().setProviderid(source.id);
				map().setLocation(source.getSelf());
			}
		});	
	}
	
	public static void MapDtoToModel(hms.dto.ProviderTracking source, ProviderTracking  dest) {	
		modelMapper.map(source, dest);
	}	
	public static void MapModelToDto(ProviderTracking source, hms.dto.ProviderTracking dest) {	
		modelMapper.map(source, dest);
	}
}
