package bbstilson

import utest.*

object SesUtilSpec extends TestSuite:

  val tests = Tests {

    test("mkBody") {
      test("properly format the message body") {
        val expectedBody = List(
          "<h2>Sausage</h2>",
          """<a href="https://trello.com/c/abc123/12-sausage">View on Trello.</a>""",
          "<br>",
          "<strong><p>Ingredients:</p></strong>",
          "<p>1 Tbsp olive oil<br>4 veggies sausages</p>",
          "<h2>bananas</h2>",
          """<a href="aksdjfkasdf">View on Trello.</a>""",
          "<br>",
          "<strong><p>Ingredients:</p></strong>",
          "<p>",
          "2 cloves garlic<br>3 ribs celery<br>15 oz can black beans<br>1/4 tsp cayenne pepper",
          "</p>"
        ).mkString
        val meals = List(
          Meal(
            id = "56f2214727212e",
            name = "Sausage",
            url = "https://trello.com/c/abc123/12-sausage",
            desc = """### INGREDIENTS
                     |1 Tbsp olive oil
                     |4 veggies sausages
                     |### DIRECTIONS
          """.stripMargin.trim
          ),
          Meal(
            id = "asdf",
            name = "bananas",
            url = "aksdjfkasdf",
            desc = """### INGREDIENTS
                     |
                     |2 cloves garlic
                     |3 ribs celery
                     |15 oz can black beans
                     |1/4 tsp cayenne pepper
                     |
                     |
                     |### DIRECTIONS
                     |
                     |1) Mince the garlic.""".stripMargin
          )
        )
        SesUtil.mkBody(meals) ==> expectedBody
      }
    }
  }
