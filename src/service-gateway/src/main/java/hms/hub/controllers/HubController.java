package hms.hub.controllers;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import hms.hub.IHubService;

@Controller
@RequestMapping("/hub")
public class HubController {
    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
	
	private IHubService hubservice;
	
	public HubController(IHubService hubservice) {
		this.hubservice = hubservice;
	}
	

    public String index() {   
    	return "index";
    }
    
    
    public String enable(UUID hubid) {    	
    	if(hubid!=null) {
	        this.hubservice.disable(hubid);
    	}
    	return this.index();
    }  
    
    public String disable(UUID hubid) {    	
    	if(hubid!=null) {
	        this.hubservice.enable(hubid);
    	}
    	return this.index();
    } 
}
