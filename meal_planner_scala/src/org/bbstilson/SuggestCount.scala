package org.bbstilson

import argonaut._, Argonaut._

case class SuggestCount(mealId: String, count: Int)

object SuggestCount {

  implicit def SuggestCountCodec = {
    casecodec2(SuggestCount.apply, SuggestCount.unapply)("mealId", "count")
  }
}
