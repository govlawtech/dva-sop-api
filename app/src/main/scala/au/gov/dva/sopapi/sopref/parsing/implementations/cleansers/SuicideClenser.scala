package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.{GenericTextClenser, MiscRegexes}

object SuicideRHClenser extends GenericTextClenser {

  private val toReplace =
    "Note: See Section 6\\s*Definitions"
  // missing number for definitions section
  override def clense(rawText: String): String = {
    val clensed = super.clense(rawText)
    val withTypoCorrected = clensed.replaceFirst(toReplace,"1 Definitions")
    return withTypoCorrected
  }

}
