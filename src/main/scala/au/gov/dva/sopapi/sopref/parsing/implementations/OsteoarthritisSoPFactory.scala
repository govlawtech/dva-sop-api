package au.gov.dva.sopapi.sopref.parsing.implementations

import java.time.LocalDate

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, ICDCode, SoP}
import au.gov.dva.sopapi.sopref.parsing.implementations.LsSoPFactory.buildFactorObjects
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory

object OsteoarthritisSoPFactory extends SoPFactory {

  override def create(registerId : String, cleansedText: String): SoP = {
    val extractor = new LsExtractor();
    val citation = OsteoarthritisParser.parseCitation(extractor.extractCitation(cleansedText));
    val instrumentNumber = OsteoarthritisParser.parseInstrumentNumber(citation);

    val definedTermsList: List[DefinedTerm] = OsteoarthritisParser.parseDefinitions(extractor.extractDefinitionsSection(cleansedText))

    val factorsSection: (Int, String) = extractor.extractFactorSection(cleansedText)
    val factors: (StandardOfProof, List[(String, String)]) = OsteoarthritisParser.parseFactors(factorsSection._2)

    val factorObjects = this.buildFactorObjects(factors._2,factorsSection._1,definedTermsList)

    val startAndEndOfAggravationParas = OsteoarthritisParser.parseStartAndEndAggravationParas(extractor.extractAggravationSection(cleansedText))
    val splitOfOnsetAndAggravationFactors = this.splitFactors(factors._2.map(_._1),startAndEndOfAggravationParas._1,startAndEndOfAggravationParas._2)

    val onsetFactors = buildFactorObjects(
      factors._2.filter(f =>  splitOfOnsetAndAggravationFactors._1.contains(f._1)),
      factorsSection._1,
      definedTermsList)

    val aggravationFactors = buildFactorObjects(
      factors._2.filter(f =>  splitOfOnsetAndAggravationFactors._2.contains(f._1)),
      factorsSection._1,
      definedTermsList)

    val effectiveFromDate: LocalDate = OsteoarthritisParser.parseDateOfEffect(extractor.extractDateOfEffectSection(cleansedText))

    val standardOfProof = factors._1

    val icdCodes: List[ICDCode] = extractor.extractICDCodes(cleansedText)

    val conditionName = OsteoarthritisParser.parseConditionNameFromCitation(citation);

    new ParsedSop(registerId,instrumentNumber,citation,aggravationFactors, onsetFactors, effectiveFromDate,standardOfProof,icdCodes,conditionName)
  }
}
