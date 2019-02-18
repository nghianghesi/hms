// @GENERATOR:play-routes-compiler
// @SOURCE:D:/MUM/HMS/service/conf/provider.routes
// @DATE:Mon Feb 18 11:55:42 ICT 2019

package provider

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset
import _root_.play.libs.F

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:2
  ProviderController_0: provider.ProviderController,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:2
    ProviderController_0: provider.ProviderController
  ) = this(errorHandler, ProviderController_0, "/")

  def withPrefix(addPrefix: String): Routes = {
    val prefix = play.api.routing.Router.concatPrefix(addPrefix, this.prefix)
    provider.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, ProviderController_0, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """provider.ProviderController.index()"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:2
  private[this] lazy val provider_ProviderController_index0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val provider_ProviderController_index0_invoker = createInvoker(
    ProviderController_0.index(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "provider",
      "provider.ProviderController",
      "index",
      Nil,
      "GET",
      this.prefix + """""",
      """""",
      Seq()
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:2
    case provider_ProviderController_index0_route(params@_) =>
      call { 
        provider_ProviderController_index0_invoker.call(ProviderController_0.index())
      }
  }
}
