package org.bbstilson

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MealSpec extends AnyFlatSpec with Matchers {
  "parseIngredients" should "parse ingredients" in {
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

    Meal.parseIngredients(desc) shouldBe List(
      "2 cloves garlic",
      "3 ribs celery",
      "15 oz can black beans",
      "1/4 tsp cayenne pepper"
    )
  }

  it should "not barf on unexpected descriptions" in {
    val malformed = """##ingredients##
                      |2 Tbsp olive oil
                      |2 cloves garlic
                      |1 yellow onion
                      |
                      |
                      | ##Directions##
                      | 1. Mince the gorbage""".stripMargin

    Meal.parseIngredients(malformed) shouldBe List(
      "2 Tbsp olive oil",
      "2 cloves garlic",
      "1 yellow onion"
    )

  }

  it should "return a nice message if there is not description" in {
    Meal.parseIngredients("") shouldBe List(Meal.NO_DESC_MSG)
  }
}
