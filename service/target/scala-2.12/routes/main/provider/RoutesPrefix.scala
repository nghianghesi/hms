// @GENERATOR:play-routes-compiler
// @SOURCE:D:/MUM/HMS/service/conf/provider.routes
// @DATE:Mon Feb 18 11:55:42 ICT 2019


package provider {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
