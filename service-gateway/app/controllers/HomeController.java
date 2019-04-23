package controllers;


import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.Settings;

import hms.common.ServiceWaiter;
import hms.provider.ProviderService;
import play.mvc.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
	
	private class pack{
		int count = 1;
	}
    public CompletableFuture<Result> index() {
    	CompletableFuture<Result> action = CompletableFuture.supplyAsync(() -> {
			return ok(views.html.index.render());
    	});
    	
    	pack p = new pack();
    	return ServiceWaiter.getInstance().waitForSignal(action,20000,()->{
    		logger.info("checking for signal");
    		return ++p.count > 100;
    	}).thenApplyAsync((r)->{
    		logger.info("Got signal");
    		return r;
    	});
    }

}
