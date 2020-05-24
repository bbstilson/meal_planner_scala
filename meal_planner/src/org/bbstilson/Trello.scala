package org.bbstilson

import argonaut._, Argonaut._
import sttp.client._
import zio._

object Trello {
  import MealCodec._

  implicit private val backend = HttpURLConnectionBackend()

  val FAILURE = "Trello API request failed, or response was not deserializable."

  def getMeals(config: TrelloConfig): Task[List[Meal]] = Task.effect {
    val url = s"https://api.trello.com/1/lists/${config.listId}/cards"
    basicRequest
      .get(uri"$url")
      .send // send http request
      .body // Either[String, String] (Left: error message, Right: response body)
      .toOption // Ignore error message.
      .flatMap(_.decodeOption[List[Meal]])
      .getOrElse(Nil)
  }
}
