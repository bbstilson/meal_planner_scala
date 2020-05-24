package org.bbstilson

import argonaut._, Argonaut._
import sttp.client._
import zio._

class Trello(config: TrelloConfig) {
  import MealCodec._

  val url = s"${config.urlBase}/lists/${config.listId}/cards"
  val auth = Map("key" -> config.apiKey, "token" -> config.token)
  implicit val backend = HttpURLConnectionBackend()

  def getMealsById(): Task[Map[String, Meal]] = Task.effect {
    basicRequest
      .get(uri"$url")
      .send // send http request
      .body // Either[String, String] (Left: error message, Right: response body)
      .toOption // Ignore error message.
      .flatMap(_.decodeOption[List[Meal]])
      .getOrElse(Nil)
      .foldLeft(Map.empty[String, Meal]) { case (map, m) => map + (m.id -> m) }
  }
}
