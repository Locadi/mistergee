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

  // http://ags.misterboo.de/api/v2/?cmd=bl

  // http://ags.misterboo.de/api/v2/?cmd=lk&id=09

  // http://ags.misterboo.de/api/v2/?cmd=gm&id=09173

  // http://ags.misterboo.de/api/v2/?cmd=gmd&id=09173112

  //  def index = Action.async {
  //    collect(List("bl", "lk", "gm"))//, "gmd"))
  //    .map{
  //      items =>
  //        Ok(views.html.index("Your new application is ready.", items))
  //    }
  //  }

  val form = Form(
    "q" -> text
  )

  def query = Action.async { implicit request =>
    val q: String = form.bindFromRequest.get
    collect(q).map {
      items =>
        Ok(views.html.results("Results", items))
    }
  }


  def index = Action {
    Ok(views.html.index("Hello"))
  }


  case class OsmObject(osmId: String, displayName: String, areaId: String, osmClass: String, osmType: String)

  def collect(q: String): Future[List[OsmObject]] = {
    WS.url("http://nominatim.openstreetmap.org/search").withQueryString("q" -> q, "format" -> "json").get().map {
      response =>
        Logger.info(response.body.toString)
        //response.json.as[JsArray].value.filter(i => i.\("class").as[String] == "boundary" && i.\("type").as[String] == "administrative").map {
        response.json.as[JsArray].value.filter(i => i.\("osm_id").asOpt[String].isDefined && i.\("display_name").asOpt[String].isDefined).map {
          item =>
            val osmId = item.\("osm_id").as[String]
            val areaId = 3600000000L+osmId.toLong
            OsmObject(
              osmId,
              item.\("display_name").as[String],
              if (areaId >= 3700000000L) "" else areaId.toString,
              item.\("class").asOpt[String].getOrElse("N/A"),
              item.\("type").asOpt[String].getOrElse("N/A"))
        }.toList
    }
  }


  //  def collect(cmds: List[String], id: Option[String] = None): Future[List[(String, String)]] = {
  //    WS.url("http://ags.misterboo.de/api/v2/").withQueryString("cmd" -> cmds.head, "id"-> id.getOrElse("")).get().flatMap {
  //      response =>
  //        try {
  //          // add the current object to the result list
  //          val parent = (response.json.\("inf3")(0).\("osm_id").as[String], response.json.\("inf3")(0).\("name").as[String])
  //
  //          // iterate over child ids and call collect
  //          val childrenIds = if (cmds.tail.isEmpty) List()
  //          else response.json.\("inf1").as[JsArray].value
  //            .map(_.\("optionValue").as[String])
  //            .filter(_.nonEmpty)
  //            .toList
  //
  //          Future.traverse(childrenIds)(id => collect(cmds.tail, Some(id))).map(children => parent :: children.flatten)
  //        } catch {
  //          case e:
  //            Exception =>
  //              Logger.error(response.body.toString)
  //          throw new Exception("BÄM")
  //        }
  //    }
  //  }

}