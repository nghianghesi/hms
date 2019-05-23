package hms.dto;

import java.util.UUID;

public class Provider {
	private UUID providerid;
	private String name;
	private String zone;
	
	public Provider()
	{		
	}
	
	public Provider(UUID providerid, String zone, String name) {
		this.providerid = providerid;
		this.name = name;
		this.zone = zone;
	}

	public UUID getProviderid() {
		return providerid;
	}
	
	public String getName() {
		return name;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public void setProviderid(UUID providerid) {
		this.providerid = providerid;
	}

	public void setName(String name) {
		this.name = name;
	}	
}
