package org.bbstilson

// import software.amazon.awssdk.services.ses.SesAsyncClient

class SesUtil(config: SesConfig) { //client: SesAsyncClient,

  def sendEmail(meals: List[Meal]): Unit = {
    println("Sending email.")
    println(config.recipients)
    meals.foreach(println)
    // response = client.send_email(
    //         Source=self.from_email,
    //         Destination={ 'ToAddresses': self.to_emails },
    //         Message={
    //             'Subject': {
    //                 'Data': self._mk_subject(),
    //                 'Charset': 'utf8'
    //             },
    //             'Body': {
    //                 'Html': {
    //                     'Data': self._mk_body(meals),
    //                     'Charset': 'utf8'
    //                 }
    //             }
    //         },
    //         ReplyToAddresses=[ self.from_email ]
    //     )
    println(s"MessageId = ${"response"}") //['MessageId']
  }
}

object SesUtil {

  def mkSubject(): String = {
    // now = datetime.now().strftime('%m/%d/%Y')
    // return f'Your weekly meal plan is here! - {now}'
    ""
  }

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
