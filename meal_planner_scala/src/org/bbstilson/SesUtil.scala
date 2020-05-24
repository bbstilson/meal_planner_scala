package org.bbstilson

import software.amazon.awssdk.services.ses.SesAsyncClient
import software.amazon.awssdk.services.ses.model._
import zio._

import scala.jdk.CollectionConverters._

class SesUtil(client: SesAsyncClient, config: SesConfig) {
  import SesUtil._

  def sendEmail(meals: List[Meal]): Task[SendEmailResponse] = {

    IO.effectAsync[Throwable, SendEmailResponse] { cb =>
      val destination = Destination.builder.toAddresses(config.recipients.asJava).build
      val subjectContent = Content.builder.charset(CHARSET).data(SUBJECT).build
      val htmlContent = Content.builder.charset(CHARSET).data(mkBody(meals)).build
      val body = Body.builder.html(htmlContent).build
      val message = Message.builder.subject(subjectContent).body(body).build
      val request = SendEmailRequest.builder
        .source(config.recipients.head)
        .destination(destination)
        .message(message)
        .replyToAddresses(config.recipients.head)
        .build

      client.sendEmail(request).handle[Unit] {
        case (response, err) =>
          err match {
            case null => cb(IO.succeed(response))
            case ex   => cb(IO.fail(ex))
          }
      }
    }
  }
}

object SesUtil {

  val SUBJECT = "Your weekly meal plan is here!"
  val CHARSET = "UTF-8"

  def mkBody(meals: List[Meal]): String = {
    meals.flatMap { meal =>
      List(
        s"<h2>${meal.name}</h2>",
        s"""<a href="${meal.url}">View on Trello.</a>""",
        "<br>",
        "<strong><p>Ingredients:</p></strong>",
        "<p>",
        Meal.parseIngredients(meal.desc).mkString("<br>"),
        "</p>"
      )
    }.mkString
  }
}
