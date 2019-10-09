package hms.provider.controllers;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hms.dto.Provider;
import hms.hub.IHubService;
import hms.provider.IAsynProviderService;
import hms.provider.IProviderInitializingService;


@RestController
@RequestMapping("/api/provider")
public class ProviderController{	
	private final int QueryDistance = 1000;
	
    private IAsynProviderService providerserivce;
    private IHubService hubservice;
    private IProviderInitializingService initalizer;

    public ProviderController(IAsynProviderService providerservice) {
    	this.providerserivce = providerservice;
    }
    
    public String index() {
        return "Provider services";
    }           
    
    public String splitHubs(UUID hubid, Integer parts) {    	
    	if(hubid!=null && parts!=null) {
	        this.hubservice.split(hubid, parts);
	        return "Hub splitted";
    	}else {
    		return "Invalid params";
    	}
    }      
    
    public CompletionStage<String> initprovider(hms.dto.Provider providerdto) {
    	return this.initalizer.initprovider(providerdto).thenApplyAsync( t ->{
    		if(t) {
    			return "init provider";
    		}else {
    			return "init provider failed";
    		}
    	});
    }
    
    public CompletionStage<String> clear(String zone) {
        return this.initalizer.clearByZone(zone).thenApplyAsync(t->{
        	if(t) {
        		return "Clear";
        	}else {
        		return "Clear failed";
        	}
        });
    }
    
    public CompletionStage<Boolean> tracking(hms.dto.ProviderTracking trackingdto) {    	
    	return this.providerserivce.asynTracking(trackingdto);
    }
    
    
    public CompletionStage<? extends List<Provider>> geoquery(hms.dto.Coordinate position) {
    	hms.dto.GeoQuery query = new hms.dto.GeoQuery(position.getLatitude(),position.getLongitude(),QueryDistance);
    	return this.providerserivce.asynQueryProviders(query);
    }
    
    public CompletionStage<List<hms.dto.Provider>> getByZone(String zone){
    	return initalizer.loadByZone(zone);
    }
}
