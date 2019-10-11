package hms.hub;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.yaml.snakeyaml.Yaml;

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
	
	private String readDeployYaml(UUID hubid) {
		File file = new File(getClass().getClassLoader().getResource("processor-tpl.yaml").getFile());
		try {
			String yaml = FileUtils.fileRead(file);
			return StringUtil.replace(yaml, "{hubid}", hubid.toString());
		} catch (IOException e) {
			return null;
		}
	}

	private void syn(HubDTO root, DeploymentList deployments) {			
		String devname = this.processorprefix+root.getHubid();
		if(root.getIsActive()) {
			if(!deployments.getItems().stream().anyMatch(d -> d.getMetadata().getName().equals(devname))) {				
				Yaml yaml = new Yaml();
				Deployment devploy = yaml.load(readDeployYaml(root.getHubid()));
				client.apps().deployments().create(devploy);
			}
			for(HubDTO sub : root.getSubHubs()) {
				syn(sub, deployments);
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
