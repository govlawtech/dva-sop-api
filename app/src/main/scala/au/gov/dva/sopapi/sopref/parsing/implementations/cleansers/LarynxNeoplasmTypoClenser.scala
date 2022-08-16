package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

object LarynxNeoplasmTypoClenser extends GenericTextClenser {

  val toReplace = "(No. 41of 2022)"
  val replacement = "(No. 41 of 2022)"

  override def clense(rawText: String): String = super.clense(rawText).replace(toReplace,replacement)
}
