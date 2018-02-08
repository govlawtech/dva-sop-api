package au.gov.dva.sopapi.sopref.parsing.traits

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, InstrumentNumber}
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{ParsedDefinedTerm, ParsedInstrumentNumber}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PreAug2015DefinitionsParsers

trait PreAugust2015SoPParser extends SoPParser with PreAug2015FactorsParser  {

  def extractStandardOfProofFromHeader(headerText: String): StandardOfProof = {
    if (headerText.contains("balance of probabilities"))
      return StandardOfProof.BalanceOfProbabilities
    if (headerText.contains("reasonable hypothesis"))
      return StandardOfProof.ReasonableHypothesis
     else {
      throw new SopParserRuntimeException("Cannot determine standard of proof from text: " + headerText)
    }
  }


  override def parseInstrumentNumber(citationSection: String): InstrumentNumber = {
    val instrumentNumberRegex = """No\.?\s+([0-9]+)\s+of\s+([0-9]{4,4})""".r
    val regexMatch = instrumentNumberRegex.findFirstMatchIn(citationSection)
    if (regexMatch.isEmpty)
      throw new SopParserRuntimeException("Cannot determine instrument number from this citation: " + citationSection)

    val number = regexMatch.get.group(1).toInt
    val year = regexMatch.get.group(2).toInt
    new ParsedInstrumentNumber(number.toInt, year.toInt)
  }

  override def parseDefinitions(definitionsSection: String): List[DefinedTerm] = {
    PreAug2015DefinitionsParsers.splitToDefinitions(definitionsSection)
      .map(PreAug2015DefinitionsParsers.parseSingleDefinition(_))
      .map(t => new ParsedDefinedTerm(t._1, t._2))
  }

  override def parseDateOfEffect(dateOfEffectSection: String): LocalDate = {
    val doeRegex = """effect from ([0-9]+\s+[A-Za-z]+\s+[0-9]{4,4})""".r
    val m = doeRegex.findFirstMatchIn(dateOfEffectSection)
    if (m.isEmpty)
      throw new SopParserRuntimeException("Cannot determine date of effect from: " + dateOfEffectSection)
    return LocalDate.parse(m.get.group(1), DateTimeFormatter.ofPattern("d MMMM yyyy"))
  }

  // Used for single aggravation paragraph (eg. Paragraph 6(b) applies)
  override def parseAggravationPara(aggravationSection: String): (String, String) = {
    val paraIntervalRegex = """Paragraph [0-9]+(\([a-z]+\))""".r
    val m = paraIntervalRegex.findFirstMatchIn(aggravationSection)
    if (m.isEmpty)
      throw new SopParserRuntimeException("Cannot determine aggravation para from: " + aggravationSection)
    (m.get.group(1), m.get.group(1))
  }


  // Used for aggravation interval (eg. Paragraphs 6(u) to 6(nn) apply)
  override def parseStartAndEndAggravationParas(aggravationSection: String): (String, String) = {
    val paraIntervalRegex = """Paragraphs [0-9]+(\([a-z]+\)) (to|and) [0-9]*(\([a-z]+\))""".r
    val intervalMatch = paraIntervalRegex.findFirstMatchIn(aggravationSection)
    if (intervalMatch.isDefined) return (intervalMatch.get.group(1), intervalMatch.get.group(3))

    val singleParaRegex = """Paragraph [0-9]+(\([a-z]+\))""".r
    val singleParaMatch = singleParaRegex.findFirstMatchIn(aggravationSection)
    if (singleParaMatch.isDefined) {
      val para = singleParaMatch.get.group(1)
      return (para,para)
    }
    else
      throw new SopParserRuntimeException("Cannot determine aggravation paras from: " + aggravationSection)
  }


  override def parseCitation(citationSection: String): String = {
    val regex = """(Statement of Principles .*)""".r
    val m = regex.findFirstMatchIn(citationSection)
    if (m.isEmpty)
      throw new SopParserRuntimeException("Cannot get citation from: " + citationSection)
    val trimmed = m.get.group(1).stripSuffix(".")
    trimmed
  }

  override def parseConditionNameFromCitation(citation: String): String = {
    val hyphenMinusFollowedBySpaceRegex = """- """.r // for some reason the raw text sometimes contains a space after unicode 055
    val regex = """Statement of Principles concerning (([A-Za-z-,'&\s0-9/](?!No\.))*)""".r

    val m = regex.findFirstMatchIn(citation)
    if (m.isEmpty)
      throw new SopParserRuntimeException("Cannot get condition name from this citation: %s".format(citation))
    val rawName = m.get.group(1)
    val withSpaceAfterHyphenMinusReplaced = rawName.replaceAll(hyphenMinusFollowedBySpaceRegex.regex,"-")
    return withSpaceAfterHyphenMinusReplaced.trim()
  }

  override def parseFactors(factorsSection: String) = {
     parseFactorsSection(factorsSection)
  }


}
















