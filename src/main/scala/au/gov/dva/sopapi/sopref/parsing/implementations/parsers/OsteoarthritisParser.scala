package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import au.gov.dva.sopapi.sopref.parsing.traits.PreAugust2015SoPParser

import scala.collection.immutable.Seq

object OsteoarthritisParser extends PreAugust2015SoPParser {

  // Cater for ';' not followed by 'or' and ';' followed by 'or' but with only one space
  def factorTextParser : Parser[String] = """(([A-Za-z0-9\-'’,\)\(\s]|\.(?=[A-Za-z0-9])|;(?!\s+or)|;(?=\s+or\s(?=\())))+""".r

  override def periodTerminator : Parser[String] = """\.""".r

  // At least two spaces following 'or' indicates end of factor
  def factorTerminator : Parser[String] = """;\s+or(\s){2,}""".r

  override def paraAndTextParser : Parser[(String, String)] = paraLetterParser ~ factorTextParser ^^ {
    case para ~ text => (para, text)
  }

  override def separatedFactorListParser : Parser[List[(String,String)]] = repsep(paraAndTextParser, factorTerminator) ^^ {
    case listOfFactors: Seq[(String, String)] => listOfFactors
  }

}
