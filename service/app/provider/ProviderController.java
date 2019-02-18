package provider;

import play.mvc.Controller;
import play.mvc.Result;

public class ProviderController  extends Controller {
    public Result index() {
        return ok("Provider controller");
    }
}
