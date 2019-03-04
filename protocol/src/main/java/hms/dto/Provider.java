package hms.dto;

import java.util.UUID;

public class Provider {
	private UUID providerid;
	private String name;
	
	public Provider()
	{
		
	}
	
	public Provider(UUID providerid, String name) {
		this.providerid = providerid;
		this.name = name;
	}

	public UUID getProviderid() {
		return providerid;
	}
	public String getName() {
		return name;
	}

	public void setProviderid(UUID providerid) {
		this.providerid = providerid;
	}

	public void setName(String name) {
		this.name = name;
	}	
}
