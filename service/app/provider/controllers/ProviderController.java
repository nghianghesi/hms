package provider.controllers;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import provider.dataaccess.IProviderRepository;
import provider.models.ProviderTracking;

public class ProviderController  extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);
    private IProviderRepository dao;
    @Inject
    public ProviderController(IProviderRepository dao) {
    	this.dao = dao;
    }
    
    public Result index() {
        return ok("Provider services");
    }    
    
    public Result tracking(Http.Request request) {
    	JsonNode json = request.body().asJson();
    	hms.dto.ProviderTracking trackingdto = Json.fromJson(json, hms.dto.ProviderTracking.class);
    	ProviderTracking tracking = this.dao.LoadById(trackingdto.id);
    	if(tracking==null) {
    		tracking = new ProviderTracking();
    	}
    	ProviderTracking.MapDtoToModel(trackingdto, tracking);
    	this.dao.Save(tracking);
        return ok("Provider tracking");
    }
}
