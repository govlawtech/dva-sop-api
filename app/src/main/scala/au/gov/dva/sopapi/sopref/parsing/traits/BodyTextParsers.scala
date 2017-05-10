package au.gov.dva.sopapi.sopref.parsing.traits

import scala.util.parsing.combinator.RegexParsers

trait BodyTextParsers extends RegexParsers {

  def mainFactorBodyText: Parser[String] = """(([A-Za-z0-9\-'’,\)\(\s]|\.(?=[A-Za-z0-9])))+""".r

  def subParaBodyText: Parser[String] = """([a-z0-9-,\s]|\.(?=[A-Za-z0-9]))+""".r
}
