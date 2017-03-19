package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import java.time.LocalDate

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, InstrumentNumber}
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, ParsedDefinedTerm}
import au.gov.dva.sopapi.sopref.parsing.traits.{PreAugust2015SoPParser, SoPParser}

object PostAugust2015Parser extends SoPParser {
  override def parseFactors(factorsSection: String): (StandardOfProof, List[FactorInfo]) = ???

  override def parseInstrumentNumber(citationSection: String): InstrumentNumber = PreAugust2015Parser.parseInstrumentNumber(citationSection)

  override def parseDefinitions(definitionsSection: String): List[DefinedTerm] = {
    PostAug2015DefinitionsParsers.splitToDefinitions(definitionsSection)
      .map(PostAug2015DefinitionsParsers.parseSingleDefinition(_))
      .map(t => new ParsedDefinedTerm(t._1, t._2))
  }

  override def parseDateOfEffect(dateOfEffectSection: String): LocalDate = ???

  override def parseAggravationPara(aggravationSection: String): (String, String) = ???

  override def parseStartAndEndAggravationParas(aggravationSection: String): (String, String) = ???

  override def parseCitation(citationSection: String): String = ???

  override def parseConditionNameFromCitation(citation: String): String = ???
}
