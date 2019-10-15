package hms.hub;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;

import hms.dto.HubDTO;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.Serialization;

public class KubernetesHub implements IKubernetesHub {

	@Value("${kubernetes.apiserver}")
	private String apiserver;
	@Value("${kubernetes.deploymentnametemplate}")
	private String deploymentnametemplate;
	@Value("${kubernetes.namespace}")
	private String namespace;
	private KubernetesClient client;
	@Value("${kubernetes.deploymentyaml}")
	private String deploymentyaml;
	
	@PostConstruct 
	public void initClient() {
		
		Config config = new ConfigBuilder().withMasterUrl(this.apiserver).build();
		this.client = new DefaultKubernetesClient(config);
	}
	
	private String readDeployYaml(UUID hubid) {
		try {
			String yaml = IOUtils.resourceToString(this.deploymentyaml, Charset.forName("UTF-8"));			
			return StringUtil.replace(yaml, "{hubid}", hubid.toString());
		} catch (IOException e) {
			return null;
		}
	}

	private void syn(HubDTO root, DeploymentList deployments) {			
		String devname = StringUtil.replace(this.deploymentnametemplate, "{hubid}", root.getHubid().toString());
		if(root.getIsActive()) {
			if(!deployments.getItems().stream().anyMatch(d -> d.getMetadata().getName().equals(devname))) {				
				Deployment devploy = Serialization.unmarshal(readDeployYaml(root.getHubid()), Deployment.class);
				client.apps().deployments().inNamespace(this.namespace).create(devploy);
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
	
	

	@Override
	public String testapi() {
		return this.client.pods().inNamespace(this.namespace).list().toString();
	}
}
