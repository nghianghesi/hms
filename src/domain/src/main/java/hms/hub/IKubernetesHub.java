package hms.hub;

import hms.dto.HubDTO;

public interface IKubernetesHub {
	public void syn(HubDTO root);
	public String testapi();
}
