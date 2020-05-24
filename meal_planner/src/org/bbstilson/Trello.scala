package org.bbstilson

import argonaut._, Argonaut._
import sttp.client._
import zio._

class Trello(config: TrelloConfig) {
  import MealCodec._

  private val url = s"https://api.trello.com/1/lists/${config.listId}/cards"
  implicit private val backend = HttpURLConnectionBackend()

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
