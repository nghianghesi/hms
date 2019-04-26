package hms.provider.controllers;


import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import hms.provider.IProviderService;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;


public class ProviderController  extends Controller {	
	private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);

    private IProviderService providerserivce;
    private HttpExecutionContext ec;
    @Inject
    public ProviderController(HttpExecutionContext ec, IProviderService providerservice) {
    	this.providerserivce = providerservice;
    	this.ec = ec;
    }
    
    public Result index() {
        return ok("Provider services");
    }        
    
    public CompletionStage<Result> initprovider(Http.Request request) {
    	JsonNode json = request.body().asJson();
    	hms.dto.Provider providerdto = Json.fromJson(json, hms.dto.Provider.class);
    	return this.providerserivce.initprovider(providerdto).thenApplyAsync( t ->{
    		if(t) {
    			return ok("init provider");
    		}else {
    			return ok("init provider failed");
    		}
    	}, ec.current());
    }
    
    public CompletionStage<Result> clear() {
        return this.providerserivce.clear().thenApplyAsync(t->{
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
    	return this.providerserivce.tracking(trackingdto).thenApplyAsync(t->{
    		return ok(Json.toJson(t));
    	}, ec.current());
    }
    
    
    public CompletionStage<Result> query(Http.Request request) {
    	JsonNode json = request.body().asJson();
    	hms.dto.Coordinate position = Json.fromJson(json, hms.dto.Coordinate.class);
    	return this.providerserivce.queryProviders(position).thenApplyAsync(t->{
    		return ok(Json.toJson(t));
    	}, ec.current());
    }
}
