package hms.provider.controllers;


import java.security.InvalidKeyException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import hms.hub.IProviderService;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class ProviderController  extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);
    private IProviderService providerserivce;
    @Inject
    public ProviderController(IProviderService providerservice) {
    	this.providerserivce = providerservice;
    }
    
    public Result index() {
        return ok("Provider services");
    }        
    
    public Result initprovider(Http.Request request) {
    	JsonNode json = request.body().asJson();
    	hms.dto.Provider providerdto = Json.fromJson(json, hms.dto.Provider.class);
    	this.providerserivce.initprovider(providerdto);
        return ok("init provider");
    }
    
    public Result clear() {
        this.providerserivce.clear();
        return ok("Clear");
    }
    
    public Result tracking(Http.Request request) throws InvalidKeyException {
    	JsonNode json = request.body().asJson();
    	hms.dto.ProviderTracking trackingdto = Json.fromJson(json, hms.dto.ProviderTracking.class);
    	this.providerserivce.tracking(trackingdto);
        return ok("Provider tracking");
    }
}
