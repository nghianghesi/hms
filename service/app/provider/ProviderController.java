package provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import hms.dto.ProviderTracking;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

public class ProviderController  extends Controller {
    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);
    
    public Result index() {
        return ok("Provider index");
    }    
    
    public Result tracking(Http.Request request) {
    	JsonNode json = request.body().asJson();
    	ProviderTracking tracking = Json.fromJson(json, ProviderTracking.class);
    	logger.info("tracking " + tracking.id + json.toString());
        return ok("Provider tracking");
    }
}
