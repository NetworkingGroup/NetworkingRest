package com.networkgroup.rest

import java.io.{IOException, InputStream}

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.http.{HttpRequest, HttpRequestInitializer}
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.GuideCategoryListResponse
import com.google.gson.{JsonArray, JsonObject}

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

/**
  * Class to handle querying the youtube data api.
  *
  * @param key The key to use for authenticating.
  */
class ApiMan(key: String) {

  val transport = new NetHttpTransport
  val factory = new JacksonFactory

  val youtube: YouTube = {
    new YouTube.Builder(transport, factory, new HttpRequestInitializer {
      override def initialize(request: HttpRequest): Unit = {}
    })
      .setApplicationName("networkinggroupproject")
      .build()
  }

  /**
    * Method to get a list of categories from youtube api.
    *
    * @return The query data if query is successful or error data.
    */
  def guideCategories(): JsonObject = {
    val query = youtube.guideCategories()
      .list("snippet")
      .setRegionCode("US")
      .setKey(key)

    val out: Try[GuideCategoryListResponse] = Try(query.execute())

    val json = new JsonObject

    out match {
      case Failure(e: IOException) =>
        json.addProperty(Constants.DATA, e.getMessage)
      case Failure(t: Throwable) => throw t
      case Success(response: GuideCategoryListResponse) =>
        val array = new JsonArray
        val items = response.getItems
        items.foreach(guide => {
          val ob = new JsonObject
          ob.addProperty(Constants.ID, guide.getId)
          ob.addProperty(Constants.TITLE, guide.getSnippet.getTitle)
          array.add(ob)
        })
        json.add(Constants.DATA, array)
    }
    json
  }

  /**
    * Method to get channel statistics for channels belonging to a specified category.
    *
    * @param category  The category to search for.
    * @param pageToken An option page token.
    * @return The queried data if query is successful or error data.
    */
  def channelStats(category: String, pageToken: Option[String]): String = {
    val query = youtube.channels()
      .list("statistics")
      .setCategoryId(category)
      .setMaxResults(Constants.MAX_PER_PAGE)
      .setKey(key)

    if (pageToken.isDefined) {
      query.setPageToken(pageToken.get)
    }

    val out: Try[InputStream] = Try(query.executeAsInputStream())

    val result = out match {
      case Failure(e: IOException) =>
        val ob = new JsonObject
        ob.addProperty(Constants.DATA, e.getMessage)
        ob.toString
      case Failure(x: Throwable) => throw x
      case Success(stream: InputStream) =>
        scala.io.Source.fromInputStream(stream).mkString
    }
    result
  }

}
