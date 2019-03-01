package hms.provider.models;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import hms.provider.entities.ProviderEntity;

public class ProviderModel{	
	private hms.provider.entities.ProviderEntity entity;
	
	public ProviderModel() {
		this.entity = new ProviderEntity();
	}
	
	// constructor for loading
	public ProviderModel(hms.provider.entities.ProviderEntity entity) {
		this.entity = entity;
	}
	
	public hms.provider.entities.ProviderEntity persistance() {
		return this.entity;
	}
	
	public UUID getProviderid() {
		return entity.getProviderid();
	}
	
	public void setProviderid(UUID id) {
		this.entity.getProviderid();
	}		
	
	public String getName() {
		return this.entity.getName();
	}
	public void setName(String name) {
		this.entity.setName(name);
	}
	
	private static final ModelMapper modelMapper = new ModelMapper();
	static {
		modelMapper.addMappings(new PropertyMap<hms.dto.ProviderTracking, ProviderModel>() {			
			@Override
			protected void configure() {
				map().setProviderid(source.id);
			}
		});	
	}
	
	public static void MapDtoToModel(hms.dto.ProviderTracking source, ProviderModel  dest) {	
		modelMapper.map(source, dest);
	}	
	public static void MapModelToDto(ProviderModel source, hms.dto.ProviderTracking dest) {	
		modelMapper.map(source, dest);
	}
}
