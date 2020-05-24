package org.bbstilson

import argonaut._, Argonaut._
import better.files._
import pureconfig._
import pureconfig.generic.auto._
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.{ GetObjectRequest, PutObjectRequest }
import software.amazon.awssdk.services.ses.SesAsyncClient
import zio._

import java.util.concurrent.CompletableFuture
import scala.util.Random

object Main extends App {
  import SuggestCount._

  def app(config: Config): Task[Unit] = {
    val getObjReq = GetObjectRequest.builder
      .bucket(config.storage.bucket)
      .key(config.storage.key)
      .build

    val putObjReq = PutObjectRequest.builder
      .bucket(config.storage.bucket)
      .key(config.storage.key)
      .build

    for {
      // 0) Initialize classes and temp file.
      tempFilePath <- Task { File.temporaryFile().get.path }
      s3 <- Task { S3AsyncClient.create }
      ses <- Task { SesAsyncClient.create }

      // 1) Download suggest counts from S3.
      getResponse <- processResponse { s3.getObject(getObjReq, tempFilePath) }
      _ = println(getResponse)

      // 2) Process raw file downloaded from S3.
      prevSuggestCounts <- tempFilePath.toFile.toScala.contentAsString
        .decodeOption[List[SuggestCount]] match {
        case Some(scs) => IO.succeed(scs)
        case None      => IO.fail(new Exception("Could not deserialize suggest counts file."))
      }

      // 3) Get all meals from Trello.
      mealsById <- Trello
        .getMeals(config.trello)
        .map(_.foldLeft(Map.empty[String, Meal]) {
          case (map, meal) => map + (meal.id -> meal)
        })

      // 4) Check that all meals are in the suggest counts
      // Add missing meals, remove meals no longer in Trello.
      suggestCounts <- Task {
        val suggestMealIds = prevSuggestCounts.map(_.mealId).toSet
        val missingMeals = mealsById.keySet
          .diff(suggestMealIds)
          .map(mealId => SuggestCount(mealId, 0))
          .toList

        prevSuggestCounts.filter(sc => mealsById.contains(sc.mealId)) ++ missingMeals
      }

      // 5) Sort meals with random likeliness rating and take the top 2.
      mealIdsToSend <- Task {
        suggestCounts
          .sortBy(_.count.toDouble * Random.nextDouble())
          .take(2)
          .map(_.mealId)
      }

      // 6) Send an email.
      sesResponse <- processResponse {
        val request = SesUtil.mkSendEmailRequest(config.ses, mealIdsToSend.map(mealsById))
        ses.sendEmail(request)
      }
      _ = println(sesResponse)

      // 7) Increment the count for the meals selected, and upload to S3.
      updatedSuggestCounts <- Task {
        suggestCounts.map { sc =>
          mealIdsToSend.contains(sc.mealId) match {
            case true  => sc.copy(count = sc.count + 1)
            case false => sc
          }
        }
      }

      putResponse <- processResponse {
        tempFilePath.toFile.toScala.overwrite(updatedSuggestCounts.asJson.nospaces)
        s3.putObject(putObjReq, tempFilePath)
      }
      _ = println(putResponse)

    } yield ()
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val config = ConfigSource.defaultApplication.loadOrThrow[Config]

    app(config)
      .fold(
        err => {
          println(err)
          1
        },
        _ => 0
      )
  }

  private def processResponse[T](thunk: => CompletableFuture[T]): Task[T] = {
    IO.effectAsync[Throwable, T] { cb =>
      val fut = thunk
      fut.handle[Unit] { (response, err) =>
        err match {
          case null => cb(IO.succeed(response))
          case ex   => cb(IO.fail(ex))
        }
      }: Unit
    }
  }

}
