package demo


import java.sql
import java.sql.{DriverManager, PreparedStatement, ResultSet, Statement}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.Connection
import akka.http.scaladsl.server.Directives
import javax.script.ScriptException

import scala.language.postfixOps
//#second-spray-json-example
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.settings.ServerSettings
import spray.json.DefaultJsonProtocol
// for JSON serialization/deserialization following dependency is required:
// "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7"

import spray.json.DefaultJsonProtocol._

import scala.io.StdIn
import scala.concurrent.Future

object WebServer {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext = system.dispatcher

  var orders: List[Item] = Nil

  // domain model
  final case class Item(name: String, id: String)
  final case class ActionPerformed(action: String)
  implicit val itemFormat = jsonFormat2(Item)



  var connection:sql.Connection = _
  def ınsert(item:Item): Future[Done] = {

    val url = "jdbc:mysql://localhost:3306/postdb?useUnicode=true&useLegacyDatetimeCode=false&serverTimezone=Turkey"
    val driver = "com.mysql.cj.jdbc.Driver"
    val username = "root"
    val password = "1234"

    try {
      // make the connection
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)
      val stmt: Statement = connection.createStatement
      stmt.executeUpdate("INSERT INTO ptable " + " VALUES ('1004' ,'Simpn' ) " )
      println("basarılı")
      connection.close();

    } catch{
      case e: ScriptException => e.printStackTrace
    }
    Future { Done }
  }


  def main(args: Array[String]) {

    val route =path("hello"){

      post {
        entity(as[Item]) { item =>
          onSuccess(ınsert(item)) {performed =>
            complete(StatusCodes.Created,performed)
            //complete(s"""Person name: ${item.name} - favorite id: ${item.id}""")
          }

        }
      }
    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

  }



  //#second-spray-json-example

}

