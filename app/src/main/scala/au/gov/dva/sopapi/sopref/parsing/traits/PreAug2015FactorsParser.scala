package au.gov.dva.sopapi.sopref.parsing.traits

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, FactorInfoWithSubParas, FactorInfoWithoutSubParas}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.FallbackFactorsParser

import scala.util.Properties
import scala.util.parsing.combinator.RegexParsers


trait PreAug2015FactorsParser extends MiscRegexes {

  val mainParaLetterRegex = """\(([a-z])+\)""".r

  private def toLineList(stringWithLinebreaks: String) = {
    stringWithLinebreaks.split(platformNeutralLineEndingRegex.regex).toList
  }

  def parseFactorsSection(factorsSectionText: String): (StandardOfProof, List[FactorInfo]) = {
    val splitToLines: List[String] = toLineList(factorsSectionText)
    val (header, rest: List[String]) = SoPExtractorUtilities.splitFactorsSectionToHeaderAndRest(splitToLines)

    val standard = extractStandardOfProofFromHeader(header.mkString(" "))
    if (rest.isEmpty) {
      val singleFactor = FallbackFactorsParser.extractFactorFromFactorSectionHead(header)
      return (standard, List(singleFactor))
    }
    val parsedFactors = FallbackFactorsParser.oldStyleSmallLetterLinesToFactors(rest)
    (standard, parsedFactors)
  }

  private def extractStandardOfProofFromHeader(headerText: String): StandardOfProof = {
    if (headerText.contains("balance of probabilities"))
      return StandardOfProof.BalanceOfProbabilities
    if (headerText.contains("reasonable hypothesis"))
      return StandardOfProof.ReasonableHypothesis
    if (headerText.startsWith("The factor that must as a minimum exist in relation to the circumstances of a person's relevant service causing or materially contributing to or aggravating"))
      return StandardOfProof.ReasonableHypothesis
    else {
      throw new SopParserRuntimeException("Cannot determine standard of proof from text: " + headerText)
    }
  }

}





