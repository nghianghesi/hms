package controllers;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import hms.hub.IHubService;
import hms.provider.controllers.Result;
import play.mvc.*;

public class HubController extends Controller {
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
	
	private IHubService hubservice;
	
	@Inject
	public HubController(IHubService hubservice) {
		this.hubservice = hubservice;
	}
	

    public Result index() {   
    	return ok(views.html.hubs.render(this.hubservice.getRootHub()));
    }
    
    
    public Result enable(UUID hubid) {    	
    	if(hubid!=null) {
	        this.hubservice.
    	}else {
    		
    	}
    }  
    
    public Result disable(UUID hubid) {    	
    	if(hubid!=null) {
	        
    	}else {
    		
    	}
    } 
}
