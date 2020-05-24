package org.bbstilson

import better.files._
import pureconfig._
import pureconfig.generic.auto._
import zio._

import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import java.util.concurrent.CompletableFuture
import software.amazon.awssdk.services.s3.model.GetObjectResponse
// import software.amazon.awssdk.services.s3.S3AsyncClient

object Main extends App {

  def app(bucket: String, key: String): Task[Unit] = {
    val getObjReq = GetObjectRequest.builder.bucket(bucket).key(key).build
    for {
      tempFile <- Task { File.temporaryFile() }
      tempFilePath = tempFile.get.path
      s3 <- Task { S3AsyncClient.create }
      getObject <- IO.effectAsync[Throwable, GetObjectResponse] { cb =>
        s3.getObject(getObjReq, tempFilePath)
          .handle[Unit] {
            case (response, err) =>
              err match {
                case null => cb(IO.succeed(response))
                case ex   => cb(IO.fail(ex))
              }
          }
      }
      _ = println(tempFilePath.toFile.toScala.contentAsString)
    } yield ()
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    val config = ConfigSource.defaultApplication.loadOrThrow[Config]

    app(config.storage.bucket, config.storage.key)
      .fold(
        err => {
          println(err)
          1
        },
        _ => 0
      )
  }

  def processResponse[T](
    fut: CompletableFuture[T],
    callback: Task[T] => Unit
  ): Unit =
    fut.handle[Unit] { (response, err) =>
      err match {
        case null => callback(IO.succeed(response))
        case ex   => callback(IO.fail(ex))
      }
    }: Unit

  // def getSuggestCounts(s3: S3AsyncClient): Task[Map[String, Int]] = Task.effect(Map.empty)

  // def putSuggestCounts(s3: S3AsyncClient): Task[Unit] = Task.effect(ZIO.succeed(()))
}

/*
# 0) Initialize all the helper classes.
s3 = S3Util(BUCKET, KEY)
trello = TrelloUtil(API_KEY, TOKEN, URL_BASE, LIST_ID)
ses = SESUtil(MY_EMAIL, [ MY_EMAIL, SO_EMAIL ])

# 1) Download suggest counts from S3.
suggest_counts = s3.get_suggest_counts()

# 2) Get all meals from Trello.
meals_by_id = trello.get_meals()

# 3) Check that all meals are in the suggest_counts, adding them if they're not.
# 3.5) Delete meals that are no longer in Trello.
for meal_id in meals_by_id:
    if meal_id not in suggest_counts:
        suggest_counts[meal_id] = 0

meals_to_delete = []
for meal_id in suggest_counts:
    if meal_id not in meals_by_id:
        meals_to_delete.append(meal_id)

for mtd in meals_to_delete:
    del suggest_counts[mtd]

# 4) Give each meal a random likeliness rating: count * random.
scored = []
for meal_id, count in suggest_counts.items():
    scored.append((meal_id, float(count) * random.random()))

# 5) Sort the scored list in ascending order. Take the top 2.
prioritized = sorted(scored, key=lambda x: x[1])[:2]
fst_mid = prioritized[0][0]
snd_mid = prioritized[1][0]

# 6) Increment those two scores in the dict.
suggest_counts[fst_mid] += 1
suggest_counts[snd_mid] += 1

# 7) Send an email.
meals_to_send = [ meals_by_id[fst_mid], meals_by_id[snd_mid] ]
ses.send_email(meals_to_send)

# 8) Finally, update the suggest_counts object in s3.
s3.update_suggest_counts(suggest_counts)
 */
