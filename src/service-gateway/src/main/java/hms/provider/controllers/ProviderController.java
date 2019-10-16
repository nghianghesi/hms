package hms.provider.controllers;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import hms.dto.Provider;
import hms.hub.IHubService;
import hms.provider.IAsynProviderService;
import hms.provider.IProviderInitializingService;


@RestController
@RequestMapping("/api/provider")
public class ProviderController{	
	private final int QueryDistance = 1000;
	
	@Autowired
    private IAsynProviderService providerserivce;
	
	@Autowired
    private IHubService hubservice;
	
	@Autowired
    private IProviderInitializingService initalizer;

    
    public String index() {
        return "Provider services";
    }           
    
    @RequestMapping("/split-hubs/{hubid}/{parts}")
    public String splitHubs(@PathVariable UUID hubid, @PathVariable Integer parts) {    	
    	if(hubid!=null && parts!=null) {
	        this.hubservice.split(hubid, parts);
	        return "Hub splitted";
    	}else {
    		return "Invalid params";
    	}
    }      
    
    @PostMapping("/initprovider")
    @ResponseBody
    public CompletionStage<String> initprovider(@RequestBody hms.dto.Provider providerdto) {
    	return this.initalizer.initprovider(providerdto).thenApplyAsync( t ->{
    		if(t) {
    			return "init provider";
    		}else {
    			return "init provider failed";
    		}
    	});
    }
    
    @RequestMapping("/clear/{zone}")
    public CompletionStage<String> clear(@PathVariable String zone) {
        return this.initalizer.clearByZone(zone).thenApplyAsync(t->{
        	if(t) {
        		return "Clear";
        	}else {
        		return "Clear failed";
        	}
        });
    }
    
    @PostMapping("/tracking")
    @ResponseBody
    public CompletionStage<Boolean> tracking(@RequestBody hms.dto.ProviderTracking trackingdto) {    	
    	return this.providerserivce.asynTracking(trackingdto);
    }
    
    @PostMapping("/geoquery")
    @ResponseBody
    public CompletionStage<? extends List<Provider>> geoquery(@RequestBody hms.dto.Coordinate position) {
    	hms.dto.GeoQuery query = new hms.dto.GeoQuery(position.getLatitude(),position.getLongitude(),QueryDistance);
    	return this.providerserivce.asynQueryProviders(query);
    }
    
    @RequestMapping("/get-by-zone/{zone}")
    @ResponseBody
    public CompletionStage<List<hms.dto.Provider>> getByZone(@PathVariable String zone){
    	return initalizer.loadByZone(zone);
    }
}
