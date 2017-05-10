package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import au.gov.dva.sopapi.exceptions.SopParserError
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, FactorInfoWithoutSubParas}
import au.gov.dva.sopapi.sopref.parsing.traits.{BodyTextParsers, TerminatorParsers}

import scala.util.Properties
import scala.util.parsing.combinator.RegexParsers

object PostAug2015FactorsParser extends RegexParsers with BodyTextParsers with TerminatorParsers {

  private val mainFactorLegalRefRegex = """^(\([0-9]+[A-Z]*\))+\s""".r

  def parseComplexFactor = ???

  def parseSimpleFactor = ???

  def splitFactorListToHeadAndRest(factorSectionLines: List[String]): (List[String], List[String]) = {
    val headExceptLastLine = factorSectionLines.takeWhile(l => !l.endsWith(":"))
    val headWithLastLine = factorSectionLines.take(headExceptLastLine.size + 1)
    factorSectionLines.splitAt(headWithLastLine.size)
  }

  def splitFactorListToIndividualFactors(factorLines: List[String]): List[List[String]] = {

    def takeTillNext(lines: List[String]): List[String] = {
       lines.takeWhile(getMainFactorLegalRef(_).isEmpty)
    }

    def groupRecursively(lines: List[String], acc: List[List[String]]): List[List[String]] = {
      if (lines.isEmpty) acc

      else {
        val nextChunk = lines.head :: takeTillNext(lines.tail)

        groupRecursively(lines.drop(nextChunk.size), nextChunk :: acc)
      }
    }

    assert(mainFactorLegalRefRegex.findFirstMatchIn(factorLines.head).isDefined)
    groupRecursively(factorLines, List[List[String]]()).reverse
  }

  private def getMainFactorLegalRef(line: String) = {
    val regexMatch = mainFactorLegalRefRegex.findFirstMatchIn(line)
    if (regexMatch.isDefined) Some(regexMatch.get.group(1)) else None
  }

  def splitFactorToHeadBodyAndTail = ???

  def splitSubFactorToHeadBodyAndTail = ???

  def getParsingFunctionForFactorLines: List[String] => FactorInfo = ???

  def mainParaLetterParser : Parser[String]  = mainFactorLegalRefRegex

  private def singleLevelFactorParser : Parser[(String,String)] = mainParaLetterParser ~ mainFactorBodyText <~ (semiColonTerminator | periodTerminator ) ^^  {
    case legalRef ~ text => (legalRef,text)
  }

  def parseFactor(lines : List[String]) : FactorInfo = {
    val result = this.parseAll(this.singleLevelFactorParser,lines.mkString(" "))
    if (!result.successful){
      throw new SopParserError("Failed to parse factor: " + lines.mkString(Properties.lineSeparator))
    }
    new FactorInfoWithoutSubParas(result.get._1,result.get._2)
  }



}
