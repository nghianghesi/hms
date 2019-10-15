package hms.hub;

import hms.dto.HubDTO;

public class EmptyKubernetesHub implements IKubernetesHub{

	@Override
	public void syn(HubDTO root) {
		return;
	}

}
