package hms;

import play.*;
import play.test.Helpers;
public class AppStart {
	public AppStart() {
		Environment env = new Environment(new java.io.File("."), this.getClass().getClassLoader(), Mode.PROD);
		ApplicationLoader.Context context = ApplicationLoader.create(env);
		ApplicationLoader loader = ApplicationLoader.apply(context);
		Application app = loader.load(context);
		Helpers.start(app);
	}
	public static void main(String[] args) {
		new AppStart();
	}

}
