// @GENERATOR:play-routes-compiler
// @SOURCE:D:/MUM/HMS/service/conf/provider.routes
// @DATE:Mon Feb 18 11:55:42 ICT 2019

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
