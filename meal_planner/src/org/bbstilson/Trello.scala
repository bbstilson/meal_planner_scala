package org.bbstilson

import argonaut._, Argonaut._
import sttp.client._
import zio._

object Trello {
  import MealCodec._

  implicit private val backend = HttpURLConnectionBackend()

  val API_ERROR = "Trello API request failed."
  val DESERIALIZATION_ERROR = "Trello API response was not deserializable."

  def getMeals(config: TrelloConfig): Task[List[Meal]] = {
    val url = s"https://api.trello.com/1/lists/${config.listId}/cards"
    basicRequest
      .get(uri"$url")
      .send
      .body
      .fold(_ => IO.fail(new Exception(API_ERROR)), response => IO.succeed(response))
      .flatMap { response =>
        response.decodeOption[List[Meal]] match {
          case Some(ms) => IO.succeed(ms)
          case None     => IO.fail(new Exception(DESERIALIZATION_ERROR))
        }
      }
  }
}
