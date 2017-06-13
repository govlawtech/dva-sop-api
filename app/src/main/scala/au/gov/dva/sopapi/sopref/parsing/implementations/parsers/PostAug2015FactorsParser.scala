package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, FactorInfoWithoutSubParas}
import au.gov.dva.sopapi.sopref.parsing.traits.MiscRegexes
import com.typesafe.scalalogging.Logger

import scala.util.Properties
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

object PostAug2015FactorsParser extends MiscRegexes {

  val logger = Logger

  private val mainFactorLegalRefRegex = """^(\([0-9]+[A-Z]*\))""".r


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


  def parseFactor(lines : List[String]) : FactorInfo = {
    val result =  FallbackFactorsParser.parseFactorsSection(lines,mainFactorLegalRefRegex)
    return result;
  }

}
