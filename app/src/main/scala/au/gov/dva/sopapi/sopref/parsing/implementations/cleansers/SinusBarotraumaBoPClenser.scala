package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

object SinusBarotraumaBoPClenser extends GenericTextClenser {

  // typo: extra ' before quote at beginning of definitions section
  val toReplace = "'\"a change in the ambient barometric pressure as specified\" means"
  val replacement = "\"a change in the ambient barometric pressure as specified\" means"

  override def clense(rawText: String): String = super.clense(rawText).replace(toReplace,replacement)

}
