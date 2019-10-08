package hms.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HubDTO {
    private UUID hubid;
    private String name;
    private String zone;
    private boolean isactive;
    
    private final List<HubDTO> subHubs = new ArrayList<HubDTO>();
    
    private HubDTO() {    	
    }
    
    public static HubDTO createByIdNameZoneActiveSubs(UUID hubid, String name, String zone, boolean isactive, List<HubDTO> subHubs) {    	
    	HubDTO res = new HubDTO();
    	res.hubid = hubid;
    	res.name = name;
    	res.isactive = isactive;
    	res.subHubs.addAll(subHubs);
    	return res;
    }
    
	public UUID getHubid() {
		return hubid;
	}

	public String getName() {
		return name;
	}

	public String getZone() {
		return zone;
	}

	public boolean getIsActive() {
		return this.isactive;
	}
	
	public List<HubDTO> getSubHubs() {
		return subHubs;
	}
}
