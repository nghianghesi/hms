package hms.hub.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import hms.hub.IHubService;
import hms.hub.IKubernetesHub;

@Controller
@RequestMapping("/hub")
public class HubController {
    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
	
	@Autowired
	private IHubService hubservice;
	@Autowired
	private IKubernetesHub kubernetesHub;
	
	

	@GetMapping("/index")
    public String index(Model model) {   
		model.addAttribute("node", this.hubservice.getRootHub());
    	return "index";
    }
    
	@GetMapping("/enable/{hubid}")
    public String enable(@PathVariable UUID hubid,Model model) {    	
    	if(hubid!=null) {
        	this.kubernetesHub.syn(this.hubservice.getHubsForActive(hubid));
	        this.hubservice.enable(hubid);
    	}
    	return this.index(model);
    }  
    
	@GetMapping("/disable/{hubid}")
    public String disable(@PathVariable UUID hubid,Model model) {    	
    	if(hubid != null) {
	        this.hubservice.disable(hubid);
    	}
    	//work around to wait for current request to disabled hubs done
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	this.kubernetesHub.syn(this.hubservice.getRootHub());
    	return this.index(model);
    }
	
	@GetMapping("/testapi")
    public String testapi() {    	
    	return this.kubernetesHub.testapi();
    }
}
