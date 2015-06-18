package controllers

import play.api.Logger
import play.api.Play.current
import play.api.data.Form
import play.api.libs.json.{JsValue, JsArray}
import play.api.libs.ws.{WSRequestHolder, WS}
import play.api.mvc._

import play.api.data.Forms._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Application extends Controller {

  val form = Form(
    "q" -> text
  )

  def query = Action.async { implicit request =>
    val q: String = form.bindFromRequest.get
    collect(q).map {
      items =>
        Ok(views.html.results(items))
    }
  }

  def nominatim = Action {
    Ok(views.html.nominatim())
  }

  def ags = Action {
    Ok(views.html.ags())
  }


  def collect(q: String): Future[List[OsmObject]] = {
    WS.url("http://nominatim.openstreetmap.org/search").withQueryString("q" -> q, "format" -> "json").get().map {
      response =>
        Logger.info(response.body.toString)
        response.json.as[JsArray].value.filter {
            i => i.\("osm_id").asOpt[String].isDefined &&
            i.\("osm_id").as[String].toLong < 100000000L &&
            i.\("display_name").asOpt[String].isDefined
        }.map {
          item =>
            OsmObject(
              item.\("osm_id").as[String],
              item.\("display_name").as[String],
              item.\("class").as[String],
              item.\("type").as[String],
              item.\("lat").as[String],
              item.\("lon").as[String]
            )
        }.toList
    }
  }

  case class OsmObject(osmId: String, displayName: String, osmClass: String, osmType: String, lat: String, lon: String)

}