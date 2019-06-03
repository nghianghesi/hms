package hms;
public class AppStart {
	public static void main(String[] args) {		
		play.Environment env = new play.Environment(new java.io.File("."), AppStart.class.getClassLoader(), play.Mode.DEV);
		play.ApplicationLoader.Context context = play.ApplicationLoader.create(env);
		play.ApplicationLoader loader = play.ApplicationLoader.apply(context);	
		play.Application app = loader.load(context);
		
		Thread t = new Thread(()->{play.api.Play.start(app.asScala());});
		t.start();
		System.console().readLine();
		play.api.Play.stop(app.asScala());	
		//AppStarter.start();
	}

}
