package bbstilson

import better.files.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import pureconfig.*
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{ GetObjectRequest, PutObjectRequest }
import software.amazon.awssdk.services.ses.SesClient

import java.net.URLDecoder
import java.util.concurrent.CompletableFuture
import scala.jdk.CollectionConverters.*
import scala.util.Random
import scala.util.Using
import scala.io.Source
import software.amazon.awssdk.core.sync.RequestBody

object MealPlanner:
  def main(args: Array[String]): Unit = run(ScheduledEvent())

  given config: Config = ConfigSource.defaultApplication.loadOrThrow[Config]

  def run(event: ScheduledEvent): Unit =
    val s3 = S3Client.create()
    val ses = SesClient.create()

    // 1) Download suggest counts from S3.
    val rawPrevSuggestCounts = Using(s3.getObject(getObjectRequest)) { is =>
      Source.fromInputStream(is, "UTF-8").mkString
    }.getOrElse {
      throw new RuntimeException("Failed to download previous content from S3.")
    }

    // 2) Process raw file downloaded from S3.
    val prevSuggestCounts = decode[List[SuggestCount]](rawPrevSuggestCounts).getOrElse {
      throw new RuntimeException("Could not deserialize suggest counts file.")
    }

    // 3) Get all meals from Trello.
    val mealsById = Trello
      .getMeals(config.trello)
      .foldLeft(Map.empty[String, Meal]) { case (map, meal) =>
        map + (meal.id -> meal)
      }

    // 4) Check that all meals are in the suggest counts
    // Add missing meals, remove meals no longer in Trello.
    val suggestMealIds = prevSuggestCounts.map(_.mealId).toSet
    val missingMeals = mealsById.keySet
      .diff(suggestMealIds)
      .map(mealId => SuggestCount(mealId, 0))
      .toList

    val suggestCounts = prevSuggestCounts
      .filter(sc => mealsById.contains(sc.mealId)) ++ missingMeals

    // 5) Sort meals with random likeliness rating and take the top 2.
    val mealIdsToSend = suggestCounts
      .sortBy(_.count.toDouble * Random.nextDouble())
      .take(2)
      .map(_.mealId)

    // 6) Send an email.
    val sesResponse = ses.sendEmail(
      SesUtil.mkSendEmailRequest(config.ses, mealIdsToSend.map(mealsById))
    )

    println(s"Successfully sent emails: $sesResponse")

    // 7) Increment the count for the meals selected, and upload to S3.
    val updatedSuggestCounts = suggestCounts.map { sc =>
      if mealIdsToSend.contains(sc.mealId) then sc.copy(count = sc.count + 1) else sc
    }

    val putResponse = s3.putObject(
      putObjectRequest,
      RequestBody.fromString(updatedSuggestCounts.asJson.noSpaces)
    )

    println(s"Successfully uploaded updated suggest counts: $putResponse")

  private def getObjectRequest(using c: Config): GetObjectRequest =
    GetObjectRequest
      .builder()
      .bucket(c.storage.bucket)
      .key(c.storage.key)
      .build()

  private def putObjectRequest(using c: Config): PutObjectRequest =
    PutObjectRequest
      .builder()
      .bucket(c.storage.bucket)
      .key(c.storage.key)
      .build()
