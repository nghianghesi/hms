package controllers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.concurrent.HttpExecutionContext;
import play.mvc.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	private HttpExecutionContext httpExecutionContext;

    @Inject
    public HomeController(HttpExecutionContext ec) {
        this.httpExecutionContext = ec;
    }

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public CompletionStage<Result> index() {
    	return CompletableFuture.supplyAsync(()->{
	    	logger.info("Test log from home");
	    	return true;
    	},httpExecutionContext.current()).thenApplyAsync((r)->{
    		return ok(views.html.index.render());	    	
    	}, httpExecutionContext.current());
    }

}
