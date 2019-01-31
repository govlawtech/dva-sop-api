package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

object HypopituitarismClenser extends  GenericTextClenser {

  private val toReplaceOldToNew = Map(
    "to \\(9\\)\\(29\\)" -> "to 9(29)",
    "to \\(9\\)\\(37\\)" -> "to 9(37)"
  )


  override def clense(rawText: String): String = {
    var clensed = super.clense(rawText)
    toReplaceOldToNew.foreach(i =>  clensed = clensed.replaceFirst(i._1,i._2))
    clensed
  }

}

