package bbstilson

case class Meal(id: String, name: String, url: String, desc: String)

object Meal:
  val NO_DESC_MSG = """I couldn't parse the description 
                      |Check the ingredients by clicking the link.
  """.stripMargin.trim

  def parseIngredients(desc: String): List[String] =
    val ingredients = desc.trim
      .split("\n")
      .tail // assumes the first line is the word "ingredients"
      .takeWhile(line =>
        !line.toUpperCase.contains("DIRECTIONS") && !line.toUpperCase.contains("INSTRUCTIONS")
      )
      .map(_.trim)
      .filterNot(_.isEmpty)
      .toList

    ingredients match
      case Nil => List(NO_DESC_MSG)
      case igs => igs
