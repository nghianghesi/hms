package hms.provider.controllers;


import java.util.UUID;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Injector;

import hms.hub.IHubService;
import hms.provider.IAsynProviderService;
import hms.provider.IProviderService;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;


public class ProviderController  extends Controller {	
	private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);
	private final int QueryDistance = 1000;
	private final Injector injector; 
	
    private IAsynProviderService providerserivce;
    private HttpExecutionContext ec;
    @Inject
    public ProviderController(HttpExecutionContext ec, IAsynProviderService providerservice, Injector injector) {
    	this.injector = injector;
    	this.providerserivce = providerservice;
    	this.ec = ec;
    }
    
    public Result index() {
        return ok("Provider services");
    }           
    
    public Result splitHubs(UUID hubid, Integer parts) {    	
    	if(hubid!=null && parts!=null) {
	        this.injector.getInstance(hms.hub.IHubService.class).split(hubid, parts);
	        return ok("Hub splitted");
    	}else {
    		return ok("Invalid params");
    	}
    }      
    
    public CompletionStage<Result> initprovider(Http.Request request) {
    	JsonNode json = request.body().asJson();
    	hms.dto.Provider providerdto = Json.fromJson(json, hms.dto.Provider.class);
    	hms.provider.IProviderInitializingService initalizer= this.injector.getInstance(hms.provider.IProviderInitializingService.class);
    	return initalizer.initprovider(providerdto).thenApplyAsync( t ->{
    		if(t) {
    			return ok("init provider");
    		}else {
    			return ok("init provider failed");
    		}
    	}, ec.current());
    }
    
    public CompletionStage<Result> clear(Http.Request request) {
    	String zone = request.body().asText();
    	hms.provider.IProviderInitializingService initalizer= this.injector.getInstance(hms.provider.IProviderInitializingService.class);
        return initalizer.clearByZone(zone).thenApplyAsync(t->{
        	if(t) {
        		return ok("Clear");
        	}else {
        		return ok("Clear failed");
        	}
        }, ec.current());
    }
    
    public CompletionStage<Result> tracking(Http.Request request) {    	
    	JsonNode json = request.body().asJson();
    	hms.dto.ProviderTracking trackingdto = Json.fromJson(json, hms.dto.ProviderTracking.class);
    	return this.providerserivce.asynTracking(trackingdto).thenApplyAsync(t->{
    		return ok(Json.toJson(t));
    	}, ec.current());
    }
    
    
    public CompletionStage<Result> geoquery(Http.Request request) {
    	JsonNode json = request.body().asJson();
    	hms.dto.Coordinate position = Json.fromJson(json, hms.dto.Coordinate.class);
    	hms.dto.GeoQuery query = new hms.dto.GeoQuery(position.getLatitude(),position.getLongitude(),QueryDistance);
    	return this.providerserivce.asynQueryProviders(query).thenApplyAsync(t->{
    		return ok(Json.toJson(t));
    	}, ec.current());
    }
    
    public CompletionStage<Result> getByZone(Http.Request request){
    	String zone = request.body().asText();
    	hms.provider.IProviderInitializingService initalizer= this.injector.getInstance(hms.provider.IProviderInitializingService.class);
    	return initalizer.loadByZone(zone).thenApplyAsync(t->{
    		return ok(Json.toJson(t));
    	}, ec.current());
    }
}
