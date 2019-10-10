package hms.hub;

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;


import hms.dto.HubDTO;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesHub implements IKubernetesHub {

	@Value("${kubernetes.deployments}")
	private String deploymentsAPIpath;	
	
	@Value("${kubernetes.apiserver}")
	private String apiserver;
	@Value("${kubernetes.processorprefix}")
	private String processorprefix;
	@Value("${kubernetes.namespace}")
	private String namespace;
	private KubernetesClient client;
	
	@PostConstruct 
	public void initClient() {
		 this.client = new DefaultKubernetesClient();
	}

	private void syn(HubDTO root, DeploymentList deployments) {			
		String devname = this.processorprefix+root.getHubid();
		if(root.getIsActive()) {
			if(!deployments.getItems().stream().anyMatch(d -> d.getMetadata().getName().equals(devname))) {
				
			}
		}else { // delete inactive
			Optional<Deployment> kdev = deployments.getItems().stream().filter(d -> d.getMetadata().getName().equals(devname)).findFirst();
			if(kdev.isPresent()) {
				this.client.apps().deployments().delete(kdev.get());
			}
		}
	}
	
	@Override
	public void syn(HubDTO root) {
		DeploymentList deployments = client.apps().deployments().inNamespace(this.namespace).list();
		this.syn(root, deployments);
	}
}
