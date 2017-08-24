package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.{GenericTextClenser, MiscRegexes}

object ClenserToFixSuperscripts extends GenericTextClenser with MiscRegexes {
  val iodineRegex = """radioactive iodine \( I\)""".r
  val yttriumRegex = """Yttrium""".r
  val floatingIsotopeNumberRegex = "^(131|90)$".r

  override def clense(rawText: String): String = {
    val initial = super.clense(rawText)
    Some(initial)
      .map(insertSuperscriptsForIsotopes(_))
      .map(removeFloatingIsotopeNumbers(_))
      .map(removeFloatingIsotopeNumbers(_))
      .get
  }

  private def insertSuperscriptsForIsotopes(text: String) = {
    val iodineReplacement = "iodine ¹³¹I"
    val yttriumReplacement = "⁹⁰Yttrium"

    Some(text)
      .map(iodineRegex.replaceAllIn(_, iodineReplacement))
      .map(yttriumRegex.replaceAllIn(_, yttriumReplacement))
      .get
  }

  private def removeFloatingIsotopeNumbers(text: String) = {

    text.split(platformNeutralLineEndingRegex.regex)
      .filter(floatingIsotopeNumberRegex.findFirstIn(_).isEmpty)
      .mkString(scala.util.Properties.lineSeparator)
  }


}
