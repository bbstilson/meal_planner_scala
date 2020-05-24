package org.bbstilson

import argonaut._, Argonaut._

case class Meal(id: String, name: String, url: String, desc: String)

object Meal {
  val NO_DESC_MSG = """I couldn't parse the description 
                      |Check the ingredients by clicking the link.
  """.stripMargin.trim

  def parseIngredients(desc: String): List[String] = {
    val stripped = desc.replaceAll("#", "").trim
    val ingredients = stripped
      .split("\n")
      .map(_.trim)
      .filterNot(_.isEmpty)
      .dropWhile(_.toUpperCase == "INGREDIENTS")
      .takeWhile(_.toUpperCase != "DIRECTIONS")
      .toList

    ingredients match {
      case Nil => List(NO_DESC_MSG)
      case igs => igs
    }
  }

}

object MealCodec {

  implicit def MealCodecJson = {
    casecodec4(Meal.apply, Meal.unapply)("id", "name", "url", "desc")
  }
}
