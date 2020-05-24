package org.bbstilson

import better.files._
import pureconfig._
import pureconfig.generic.auto._
import zio._

import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.{ GetObjectRequest, GetObjectResponse }
import scala.util.Random

case class SuggestCount(mealId: String, count: Int)

object Main extends App {

  def app(config: Config): Task[Unit] = {
    val getObjReq = GetObjectRequest.builder
      .bucket(config.storage.bucket)
      .key(config.storage.key)
      .build

    for {
      // 0) Initialize classes and temp file.
      tempFilePath <- Task { File.temporaryFile().get.path }
      s3 <- Task { S3AsyncClient.create }
      trello <- Task { new Trello(config.trello) }

      // 1) Download suggest counts from S3.
      _ <- IO.effectAsync[Throwable, GetObjectResponse] { cb =>
        s3.getObject(getObjReq, tempFilePath)
          .handle[Unit] {
            case (response, err) =>
              err match {
                case null => cb(IO.succeed(response))
                case ex   => cb(IO.fail(ex))
              }
          }
      }
      // 2) Process raw file downloaded from S3.
      rawSuggestCounts <- Task {
        val fileLines: List[String] = tempFilePath.toFile.toScala.lineIterator.toList
        fileLines.map(_.split(",").toList).map {
          case List(mealId, count) => SuggestCount(mealId, count.toInt)
        }
      }

      // 3) Get all meals from Trello.
      mealsById <- trello.getMealsById()

      // 4) Check that all meals are in the suggest counts
      // Add missing meals, remove meals no longer in Trello.
      suggestCounts <- Task {
        val suggestMealIds = rawSuggestCounts.map(_.mealId).toSet
        val missingMeals = mealsById.keySet
          .diff(suggestMealIds)
          .map(mealId => SuggestCount(mealId, 0))
          .toList

        rawSuggestCounts.filter(sc => mealsById.contains(sc.mealId)) ++ missingMeals
      }
      // 5) Sort meals with random likeliness rating and take the top 2.
      mealsToSend <- Task {
        suggestCounts.sortBy(_.count.toDouble * Random.nextDouble()).take(2)
      }
      // 6) Send an email.

      // 7) Increment the count for the meals selected, and upload to S3.
      _ = mealsToSend.foreach(println)
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
}

/*
// 0) Initialize all the helper classes.
s3 = S3Util(BUCKET, KEY)
trello = TrelloUtil(API_KEY, TOKEN, URL_BASE, LIST_ID)
ses = SESUtil(MY_EMAIL, [ MY_EMAIL, SO_EMAIL ])


// 6) Increment those two scores in the dict.
suggest_counts[fst_mid] += 1
suggest_counts[snd_mid] += 1

// 7) Send an email.
meals_to_send = [ meals_by_id[fst_mid], meals_by_id[snd_mid] ]
ses.send_email(meals_to_send)

// 8) Finally, update the suggest_counts object in s3.
s3.update_suggest_counts(suggest_counts)
 */
