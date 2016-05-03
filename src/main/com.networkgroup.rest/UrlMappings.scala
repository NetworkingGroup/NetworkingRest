package com.networkgroup.rest

import com.google.gson.JsonObject
import spark.Spark._
import spark.{Request, Response, Route, Spark}

import scala.io.StdIn.readLine

/**
  * This singleton is the main entry point into the program and the starting point of the server.
  * It configures the server and maps the expected url patterns to an set of actions.
  */
object UrlMappings {

  /**
    * Implicit function to make a route object. Acts as syntactic sugar to circumvent typing new Route ......
    *
    * @param handleFunc The function to execute for the route.
    * @return A route object.
    */
  implicit def route(handleFunc: (Request, Response) => AnyRef): Route = {
    new Route {
      override def handle(request: Request, response: Response): AnyRef = {
        handleFunc(request, response)
      }
    }
  }

  /**
    * Main method entry point.
    * @param args Args for the program. [Not used]
    */
  def main(args: Array[String]): Unit = {

    Spark.staticFileLocation("/")
    port(readLine("What port to run on:").toInt)

    val key = scala.io.Source.fromInputStream(
      UrlMappings.getClass.getClassLoader.getResourceAsStream("key.txt")
    ).mkString

    get("/categories", (request: Request, response: Response) => {
      response.`type`("application/json")
      new ApiMan(key).guideCategories()
    })

    get("/channels", (request: Request, response: Response) => {
      response.`type`("application/json")
      val cat = request.queryParams("category")
      val token = request.queryParams("page")

      if (cat == null) {
        val ob = new JsonObject
        ob.addProperty("Error", "Missing param")
        ob.toString
      } else {
        new ApiMan(key).channelStats(cat, token match {
          case null => None
          case _ => Some(token)
        })
      }
    })

  }


}
