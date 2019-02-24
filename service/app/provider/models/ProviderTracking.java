package provider.models;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import xyz.morphia.annotations.Entity;
import xyz.morphia.annotations.Id;

@Entity(value = "ProviderTracking")
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
	
	public static void MapDtoToModel(hms.dto.ProviderTracking source, ProviderTracking  dest) {	
		modelMapper.map(source, dest);
	}	
	public static void MapModelToDto(ProviderTracking source, hms.dto.ProviderTracking dest) {	
		modelMapper.map(source, dest);
	}
}
