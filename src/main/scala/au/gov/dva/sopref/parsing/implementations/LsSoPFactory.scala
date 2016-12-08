package au.gov.dva.sopref.parsing.implementations

import java.time.LocalDate

import au.gov.dva.sopref.interfaces.model.{DefinedTerm, ICDCode, SoP, StandardOfProof}
import au.gov.dva.sopref.parsing.traits.SoPFactory

object LsSoPFactory extends SoPFactory{
  override def create(registerId : String, clensedText: String): SoP = {
    val extractor = new LsExtractor();
    val citation = LsParser.parseCitation(extractor.extractCitation(clensedText));
    val instrumentNumber = LsParser.parseInstrumentNumber(citation);

    val definedTermsList: List[DefinedTerm] = LsParser.parseDefinitions(extractor.extractDefinitionsSection(clensedText))

    val factorsSection: (Int, String) = extractor.extractFactorSection(clensedText)
    val factors: (StandardOfProof, List[(String, String)]) = LsParser.parseFactors(factorsSection._2)

    val factorObjects = this.buildFactorObjects(factors._2,factorsSection._1,definedTermsList)

    val startAndEndOfAggravationParas = LsParser.parseStartAndEndAggravationParas(extractor.extractAggravationSection(clensedText))
    val splitOfOnsetAndAggravationFactors = this.splitFactors(factors._2.map(_._1),startAndEndOfAggravationParas._1,startAndEndOfAggravationParas._2)

    val onsetFactors = buildFactorObjects(
      factors._2.filter(f =>  splitOfOnsetAndAggravationFactors._1.contains(f._1)),
      factorsSection._1,
      definedTermsList)

    val aggravationFactors = buildFactorObjects(
      factors._2.filter(f =>  splitOfOnsetAndAggravationFactors._2.contains(f._1)),
      factorsSection._1,
      definedTermsList)

    val effectiveFromDate: LocalDate = LsParser.parseDateOfEffect(extractor.extractDateOfEffectSection(clensedText))

    val standardOfProof = factors._1

    val icdCodes: List[ICDCode] = extractor.extractICDCodes(clensedText)

    new ParsedSop(registerId,instrumentNumber,citation,aggravationFactors, onsetFactors, effectiveFromDate,standardOfProof,icdCodes)
  }



}
