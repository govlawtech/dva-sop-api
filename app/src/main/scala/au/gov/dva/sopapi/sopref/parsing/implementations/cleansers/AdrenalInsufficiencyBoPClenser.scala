package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser


object AdrenalInsufficiencyBoPClenser extends GenericTextClenser {

  private val toReplace =
    "Note: See Section 6\\s*2\\s*Definitions"
  // missing number for definitions section
  override def clense(rawText: String): String = {
    val clensed = super.clense(rawText)
    val withTypoCorrected = clensed.replaceFirst(toReplace,"1 Definitions")
    return withTypoCorrected
  }


}



