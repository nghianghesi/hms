package controllers;


import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import hms.common.ServiceWaiter;
import hms.common.ServiceWaiter.IServiceChecker;
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
    public Result index() {    	
    	/*pack p = new pack();
    	IServiceChecker<Result> waiter = new IServiceChecker<Result>() {

			@Override
			public boolean isReady() {    		
				logger.info("checking for signal");
				return ++p.count > 1;
			}

			@Override
			public Result getResult() {
				return ok(views.html.index.render());
			}

			@Override
			public boolean isError() {
				return false;
			}

			@Override
			public Throwable getError() {
				return null;
			}
    	};
    	
    	return ServiceWaiter.getInstance()
    			.waitForSignal(waiter, 20000)
    			.thenApplyAsync((r)->{
		    		logger.info("Got signal");
		    		return r;
		    	});*/
    	return ok("hms.provider.inmem-tracking-with-hub{hubid}".replaceAll("\\{hubid\\}", "replaced"));
    }

}
