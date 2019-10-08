package controllers;


import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

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
	
	private Config conf;
	@Inject
	public HomeController(Config conf) {
		this.conf = conf;
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
    	return ok("Home"+this.conf.getString("kafka.stream.rootid"));
    }

}
