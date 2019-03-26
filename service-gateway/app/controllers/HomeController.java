package controllers;

import javax.inject.Inject;

import org.bson.types.ObjectId;

import com.mongodb.MongoClientURI;
import com.typesafe.config.Config;

import play.mvc.*;
import xyz.morphia.Datastore;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

	private Config config;Datastore s;
	@Inject
	public HomeController(Config config, Datastore s) {
		//s.get(hms.hub.entities.HubNodeEntity.class, new ObjectId("5c990886295dfe681b494cd3"));
		this.s = s;
		this.config = config;
	}
    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
    	MongoClientURI uri = new MongoClientURI(config.getString("playmorphia.uri"));
    	s.get(hms.hub.entities.HubNodeEntity.class, new ObjectId("5c990886295dfe681b494cd3"));
        return ok(uri.getDatabase() + views.html.index.render());
    }

}
