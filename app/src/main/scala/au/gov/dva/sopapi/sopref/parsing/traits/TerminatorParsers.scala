package au.gov.dva.sopapi.sopref.parsing.traits

import scala.util.parsing.combinator.RegexParsers



trait TerminatorParsers extends RegexParsers {
  def orTerminator: Parser[String] = """; or""".r

  def andTerminator: Parser[String] = """; and""".r

  def semiColonTerminator: Parser[String] = """;""".r

  def periodTerminator: Parser[String] = """\.$""".r
}
