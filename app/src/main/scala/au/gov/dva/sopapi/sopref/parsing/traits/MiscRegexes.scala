package au.gov.dva.sopapi.sopref.parsing.traits

import scala.util.parsing.combinator.RegexParsers


trait MiscRegexes {

  val platformNeutralLineEndingRegex = """(\n|\r\n)""".r
}

