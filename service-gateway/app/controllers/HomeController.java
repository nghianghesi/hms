package controllers;


import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.Settings;

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
		CompletableFuture<Result> c;
		CompletableFuture<Result> cw;
		int count = 1;
	}
    public CompletableFuture<Result> index() {
    	pack p = new pack();
    	p.c = CompletableFuture.supplyAsync(() -> {
    	String msg = "";
    	hms.dto.Coordinate t = new hms.dto.Coordinate(10,10);
    	DslJson<Object> dslJson = new DslJson<>(
    			Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());

		JsonWriter writer = dslJson.newWriter();
		try {
			dslJson.serialize(writer, t);
			byte[] buff = writer.getByteBuffer();
			msg = new String(buff);
			
			msg+=dslJson.deserialize(hms.dto.Coordinate.class, writer.getByteBuffer(), buff.length);
		}catch(IOException ex) {
			
		}
		logger.info("Final result");
		Result res = ok(msg + views.html.index.render());
        p.c.complete(res);
        p.cw.cancel(true); 
        return res;
    	});
    	
    	Function<Result,Result> fn = (r)->{
    		logger.info("inter");
    		try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}
    		return r;		
    	};
    	p.c=p.cw=p.c.thenApplyAsync(fn);
    	p.c=p.c.thenApplyAsync(fn)
    			.thenApplyAsync(fn)
    			.thenApplyAsync(fn)
    			.thenApplyAsync(fn)
    			.thenApplyAsync(fn)
    			.thenApplyAsync(fn)
    			.thenApplyAsync(fn)
    			.thenApplyAsync(fn)
    			.thenApplyAsync(fn)
    			.thenApplyAsync(fn)
    			.thenApplyAsync(fn)
    			.thenApplyAsync(fn);
    	p.c.thenApplyAsync((r)->{
    		logger.info("before render");
    		return r;
    	});
    	return p.c;
    }

}
