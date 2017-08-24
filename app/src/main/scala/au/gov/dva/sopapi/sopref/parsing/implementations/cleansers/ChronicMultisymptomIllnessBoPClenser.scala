package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

import scala.util.Properties

object ChronicMultisymptomIllnessBoPClenser extends GenericTextClenser{
  // fix typo: first definition does not start with quotation mark
  val toReplace = """6\. For the purposes of this Statement of Principles:\s*death from chronic multisymptom illness""".r
  val replacement = "6. For the purposes of this Statement of Principles:" + Properties.lineSeparator + "\"death from chronic multisymptom illness"

  override def clense(rawText: String): String = super.clense(rawText).replaceFirst(toReplace.regex,replacement)
}
