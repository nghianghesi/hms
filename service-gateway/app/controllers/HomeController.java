package controllers;


import java.io.IOException;
import java.util.UUID;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.Settings;

import hms.dto.Coordinate;
import play.mvc.*;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {
    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {

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
        return ok(msg + views.html.index.render());
    }

}
