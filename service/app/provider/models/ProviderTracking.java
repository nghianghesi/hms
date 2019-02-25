package provider.models;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Field;
import xyz.morphia.annotations.Index;
import xyz.morphia.annotations.Indexes;
import xyz.morphia.annotations.IndexOptions;

@Entity(value = "ProviderTracking")
@Indexes({
    @Index(fields = @Field("id"), options = @IndexOptions(name = "indexing_id"))
})
public class ProviderTracking{
    private UUID id;
	public double latitude;
	public double longitude;
	
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	
	private static final ModelMapper modelMapper = new ModelMapper();
	static {
		modelMapper.addMappings(new PropertyMap<hms.dto.ProviderTracking, ProviderTracking>() {			
			@Override
			protected void configure() {
				// TODO Auto-generated method stub
				map().setId(source.id);
				map().setLatitude(source.latitude);
				map().setLongitude(source.longitude);
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
