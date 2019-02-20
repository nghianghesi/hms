// @GENERATOR:play-routes-compiler
// @SOURCE:D:/MUM/HMS/service/conf/p.routes
// @DATE:Tue Feb 19 02:02:01 ICT 2019

package provider;

import p.RoutesPrefix;

public class routes {
  
  public static final provider.ReverseProviderController ProviderController = new provider.ReverseProviderController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final provider.javascript.ReverseProviderController ProviderController = new provider.javascript.ReverseProviderController(RoutesPrefix.byNamePrefix());
  }

}
