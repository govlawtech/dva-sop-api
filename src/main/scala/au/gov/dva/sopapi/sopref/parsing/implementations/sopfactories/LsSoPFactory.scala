package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import java.time.LocalDate

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, ICDCode, SoP}
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.PreAugust2015Extractor
import au.gov.dva.sopapi.sopref.parsing.implementations.model.ParsedSop
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PreAugust2015Parser
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory

object LsSoPFactory extends SoPFactory{
  override def create(registerId : String, rawText : String, cleansedText: String): SoP = {
    val extractor = PreAugust2015Extractor
    val citation = PreAugust2015Parser.parseCitation(extractor.extractCitation(cleansedText));
    val instrumentNumber = PreAugust2015Parser.parseInstrumentNumber(citation);

    val definedTermsList: List[DefinedTerm] = PreAugust2015Parser.parseDefinitions(extractor.extractDefinitionsSection(cleansedText))

    val factorsSection: (Int, String) = extractor.extractFactorSection(cleansedText)
    val factors: (StandardOfProof, List[(String, String)]) = PreAugust2015Parser.parseFactors(factorsSection._2)

    val factorObjects = this.buildFactorObjects(factors._2,factorsSection._1,definedTermsList)

    val startAndEndOfAggravationParas = PreAugust2015Parser.parseStartAndEndAggravationParas(extractor.extractAggravationSection(cleansedText))
    val splitOfOnsetAndAggravationFactors = this.splitFactors(factors._2.map(_._1),startAndEndOfAggravationParas._1,startAndEndOfAggravationParas._2)

    val onsetFactors = buildFactorObjects(
      factors._2.filter(f =>  splitOfOnsetAndAggravationFactors._1.contains(f._1)),
      factorsSection._1,
      definedTermsList)

    val aggravationFactors = buildFactorObjects(
      factors._2.filter(f =>  splitOfOnsetAndAggravationFactors._2.contains(f._1)),
      factorsSection._1,
      definedTermsList)

    val effectiveFromDate: LocalDate = PreAugust2015Parser.parseDateOfEffect(extractor.extractDateOfEffectSection(cleansedText))

    val standardOfProof = factors._1

    val icdCodes: List[ICDCode] = extractor.extractICDCodes(cleansedText)

    val conditionName = PreAugust2015Parser.parseConditionNameFromCitation(citation);

    new ParsedSop(registerId,instrumentNumber,citation,aggravationFactors, onsetFactors, effectiveFromDate,standardOfProof,icdCodes,conditionName)
  }



}
