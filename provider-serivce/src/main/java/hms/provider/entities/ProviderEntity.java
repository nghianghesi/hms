package hms.provider.entities;

import java.util.UUID;

public class ProviderEntity {
	private UUID providerid;
	private UUID hubid;
	private String name;
	
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
	public UUID getHubid() {
		return hubid;
	}
	public void setHubid(UUID hubid) {
		this.hubid = hubid;
	}
}
