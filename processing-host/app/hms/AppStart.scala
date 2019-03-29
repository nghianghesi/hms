package hms

object AppStart{
  import play.api._
  
  def main(args: Array[String]) {
    val env = Environment(new java.io.File("."), this.getClass.getClassLoader, Mode.Dev)
    val context = ApplicationLoader.createContext(env)
    val loader = ApplicationLoader(context)
    val app = loader.load(context)
    Play.start(app)  
  }
}