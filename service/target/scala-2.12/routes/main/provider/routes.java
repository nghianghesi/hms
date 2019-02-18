// @GENERATOR:play-routes-compiler
// @SOURCE:D:/MUM/HMS/service/conf/provider.routes
// @DATE:Mon Feb 18 11:55:42 ICT 2019

package provider;

import provider.RoutesPrefix;

public class routes {
  
  public static final provider.ReverseProviderController ProviderController = new provider.ReverseProviderController(RoutesPrefix.byNamePrefix());

  public static class javascript {
    
    public static final provider.javascript.ReverseProviderController ProviderController = new provider.javascript.ReverseProviderController(RoutesPrefix.byNamePrefix());
  }

}
