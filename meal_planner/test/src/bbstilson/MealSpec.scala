package bbstilson

import utest.*

object MealSpec extends TestSuite:

  val tests = Tests {
    test("parseIngredients") {
      test("parses ingredients") {
        val desc = """### INGREDIENTS
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

        Meal.parseIngredients(desc) ==> List(
          "2 cloves garlic",
          "3 ribs celery",
          "15 oz can black beans",
          "1/4 tsp cayenne pepper"
        )
      }

      test("not barf on unexpected descriptions") {
        val malformed = """##ingredients##
                          |2 Tbsp olive oil
                          |2 cloves garlic
                          |1 yellow onion
                          |
                          |
                          | ##Directions##
                          | 1. Mince the gorbage""".stripMargin

        Meal.parseIngredients(malformed) ==> List(
          "2 Tbsp olive oil",
          "2 cloves garlic",
          "1 yellow onion"
        )
      }

      test("return a nice message if there is not description") {
        Meal.parseIngredients("") ==> List(Meal.NO_DESC_MSG)
      }
    }
  }
