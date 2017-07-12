package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.interfaces.model.DefinedTerm
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, ParsedDefinedTerm}
import au.gov.dva.sopapi.sopref.parsing.traits.{PreAugust2015SoPParser, SoPParser}

import scala.util.Properties

object PostAugust2015Parser extends SoPParser with PreAugust2015SoPParser {
  override def parseFactors(factorsSection: String): (StandardOfProof, List[FactorInfo]) = {
    val (factorsSectionHead, factorsList) = PostAug2015FactorsParser.splitFactorListToHeadAndRest(
      factorsSection.split(Properties.lineSeparator).toList)

    val standardOfProof = extractStandardOfProofFromHeader(
      factorsSectionHead.mkString(Properties.lineSeparator))

    val factorSections = PostAug2015FactorsParser.splitFactorListToIndividualFactors(factorsList)

    val parsedFactors: List[FactorInfo] = factorSections.map(f => PostAug2015FactorsParser.parseFactor(f))

    (standardOfProof, parsedFactors)

  }

  override def parseDefinitions(definitionsSection: String): List[DefinedTerm] = {
    PostAug2015DefinitionsParsers.splitToDefinitions(definitionsSection)
      .map(PostAug2015DefinitionsParsers.parseSingleDefinition)
      .map(t => new ParsedDefinedTerm(t._1, t._2))
  }

  override def parseCitation(citationSection: String): String = {

    val withLineBreaksReplaced = citationSection.replaceAll(Properties.lineSeparator, " ")
    val regex = """This is the Statement of Principles concerning (.*)""".r
    val m = regex.findFirstMatchIn(withLineBreaksReplaced)
    if (m.isEmpty)
      throw new SopParserRuntimeException("Cannot get citation from: " + withLineBreaksReplaced)
    val trimmed = m.get.group(1).stripSuffix(".")
    trimmed
  }

  override def parseDateOfEffect(dateOfEffectSection: String): LocalDate = {
    val doeRegex = """This instrument commences on ([0-9]+\s+[A-Za-z]+\s+[0-9]{4,4})""".r
    val m = doeRegex.findFirstMatchIn(dateOfEffectSection)
    if (m.isEmpty)
      throw new SopParserRuntimeException("Cannot determine date of effect from: " + dateOfEffectSection)
    return LocalDate.parse(m.get.group(1), DateTimeFormatter.ofPattern("d MMMM yyyy"))
  }

  override def parseStartAndEndAggravationParas(aggravationSection: String): (String, String) = {
    val paraIntervalRegex = """subsections [0-9]+(\([0-9]+\)) to [0-9]+(\([0-9]+\))""".r
    val intervalMatch = paraIntervalRegex.findFirstMatchIn(aggravationSection)
    if (intervalMatch.isDefined) return (intervalMatch.get.group(1), intervalMatch.get.group(2))

    val singleParaRegex = """[Ss]ubsection [0-9]+(\([0-9]+\))""".r
    val singleParaMatch = singleParaRegex.findFirstMatchIn(aggravationSection)
    if (singleParaMatch.isDefined) {
      val para = singleParaMatch.get.group(1)
      return (para, para)
    }
    else
      throw new SopParserRuntimeException("Cannot determine aggravation paras from: " + aggravationSection)
  }

  override def parseConditionNameFromCitation(citation: String): String = {


    val regex = """([A-Za-z-'\s]+)\(""".r

    val m = regex.findFirstMatchIn(citation)
    if (m.isEmpty)
      throw new SopParserRuntimeException("Cannot get condition name from this citation: %s".format(citation))
    return m.get.group(1).trim()
  }

}
