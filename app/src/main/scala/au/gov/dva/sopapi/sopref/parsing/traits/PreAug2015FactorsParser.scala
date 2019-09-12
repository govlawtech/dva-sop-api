package au.gov.dva.sopapi.sopref.parsing.traits

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, FactorInfoWithoutSubParas}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.FactorsParser
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.FactorsParser.{MainPara, ParaLines}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors.{ConditionVariants, GenericSubFactorParser}

import scala.util.Properties
import scala.util.parsing.combinator.RegexParsers


trait PreAug2015FactorsParser extends MiscRegexes {

  val mainParaLetterRegex = """\(([a-z])+\)""".r

  private def toLineList(stringWithLinebreaks: String) = {
    stringWithLinebreaks.split(platformNeutralLineEndingRegex.regex).toList
  }


  def parseFactorsSection(factorsSectionText: String, paraLinesShouldBeChildrenAccordingToCustomRule : (MainPara, ParaLines) => Boolean = (_,_) => false): (StandardOfProof, List[FactorInfo]) = {

    val (header, rest: List[String]) = splitToHeaderAndRest(factorsSectionText)

    val standard = extractStandardOfProofFromHeader(header.mkString(" "))
    if (rest.isEmpty) {
      val singleFactor = FactorsParser.extractFactorFromFactorSectionHead(header)
      return (standard, List(singleFactor))
    }
    val parsedFactors = FactorsParser.oldStyleSmallLetterLinesToFactors(rest,paraLinesShouldBeChildrenAccordingToCustomRule)

    val parsedFactorsWithConditionVariants = parsedFactors.
      map(fi => ConditionVariants.addornFactor(fi,true))

    (standard, parsedFactorsWithConditionVariants)
  }

  private def splitToHeaderAndRest(factorsSectionText: String) = {
    val splitToLines: List[String] = toLineList(factorsSectionText)
    val (header, rest: List[String]) = SoPExtractorUtilities.splitFactorsSectionToHeaderAndRest(splitToLines)
    (header,rest)
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





