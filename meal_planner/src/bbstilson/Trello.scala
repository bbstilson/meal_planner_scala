package bbstilson

import sttp.client3.*
import sttp.client3.circe.*
import io.circe.generic.auto.*

object Trello:

  private val backend = HttpURLConnectionBackend()

  private val API_URL = "https://api.trello.com/1/"
  private val API_ERROR = "Trello API request failed."
  private val DESERIALIZATION_ERROR = "Trello API response was not deserializable."

  def getMeals(config: TrelloConfig): List[Meal] =
    basicRequest
      .get(uri"$API_URL/lists/${config.listId}/cards")
      .response(asJson[List[Meal]])
      .send(backend)
      .body
      .getOrElse {
        throw new Exception("Failed to deserialize response!")
      }
