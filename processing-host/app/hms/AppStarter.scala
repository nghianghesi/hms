package hms

object AppStarter{
  import play.api._
  
  var app : Application = null; 
  def start() {
    val env = Environment(new java.io.File("."), this.getClass.getClassLoader, Mode.Dev)
    val context = ApplicationLoader.createContext(env)
    val loader = ApplicationLoader(context)
    app = loader.load(context)
    Play.start(app)  
  }
  
  def stop(){    
    app.stop()
  }
}