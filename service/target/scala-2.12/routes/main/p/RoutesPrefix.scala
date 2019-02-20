// @GENERATOR:play-routes-compiler
// @SOURCE:D:/MUM/HMS/service/conf/p.routes
// @DATE:Tue Feb 19 02:02:01 ICT 2019


package p {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
