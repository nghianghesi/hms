// @GENERATOR:play-routes-compiler
// @SOURCE:D:/MUM/HMS/service/conf/p.routes
// @DATE:Tue Feb 19 02:02:01 ICT 2019

import play.api.mvc.Call


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:2
package provider {

  // @LINE:2
  class ReverseProviderController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:2
    def index(): Call = {
      
      Call("GET", _prefix)
    }
  
  }


}
