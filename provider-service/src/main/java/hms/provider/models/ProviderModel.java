package hms.provider.models;

import java.util.UUID;

import javax.annotation.concurrent.Immutable;

import hms.provider.entities.ProviderEntity;
import xyz.morphia.geo.GeoJson;
import xyz.morphia.geo.Point;

public class ProviderModel{	

	@Immutable
	public static class ProviderTrackingModel {
		private hms.provider.entities.ProviderEntity.ProviderTrackingStruct entity
		 = new hms.provider.entities.ProviderEntity.ProviderTrackingStruct();
		public ProviderTrackingModel() {
			
		}
	
		public ProviderTrackingModel(UUID hubid, double latitude, double longitude) {
			this.entity = new hms.provider.entities.ProviderEntity.ProviderTrackingStruct();
			this.entity.setHubid(hubid);
			this.entity.setLocation(GeoJson.point(latitude,longitude));
		}
		
		public UUID getHubid() {
			return this.entity.getHubid();
		}
		
		public Point getLocation() {
			return this.entity.getLocation();
		}
	}


	private hms.provider.entities.ProviderEntity entity;
	private ProviderTrackingModel previousTracking;
	private ProviderTrackingModel currentTracking;

	public ProviderModel() {
		this.entity = new ProviderEntity();
		this.entity.setProviderid(UUID.randomUUID());
	}
	
	// constructor for loading
	public static ProviderModel load(hms.provider.entities.ProviderEntity entity) {
		if(entity != null) {
			ProviderModel model = new ProviderModel() ;
			model.entity = entity;
			if(model.entity.getCurrentTracking()!=null) {
				model.currentTracking = new ProviderTrackingModel();
				model.currentTracking.entity = entity.getCurrentTracking();
			}
			return model;
		}else {
			return null;
		}
	}
	
	public void load(hms.dto.Provider dto) {
		this.setProviderid(dto.getProviderid());
		this.setName(dto.getName());
	}
	
	public hms.provider.entities.ProviderEntity persistance() {
		return this.entity;
	}
	
	public UUID getProviderid() {
		return entity.getProviderid();
	}
	
	public void setProviderid(UUID id) {
		this.entity.setProviderid(id);
	}		
	
	public String getName() {
		return this.entity.getName();
	}
	
	public void setName(String name) {
		this.entity.setName(name);
	}
	
	
	public ProviderTrackingModel getCurrentTracking() {
		return currentTracking;
	}

	public void setCurrentTracking(ProviderTrackingModel currentTracking) {
		if(this.previousTracking == null) {
			this.previousTracking = this.currentTracking;
		}
		this.currentTracking = currentTracking;
		this.entity.setCurrentTracking(currentTracking!=null?currentTracking.entity:null);
	}
}
