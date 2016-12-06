package au.gov.dva.sopref.parsing.implementations

import java.time.LocalDate

import au.gov.dva.sopref.interfaces.model.{DefinedTerm, SoP, StandardOfProof}
import au.gov.dva.sopref.parsing.traits.SoPFactory

object LsSoPFactory extends SoPFactory{
  override def create(registerId : String, clensedText: String): SoP = {
    val extractor = new LsExtractor();
    val citation = extractor.extractCitation(clensedText);
    val instrumentNumber = LsParser.parseInstrumentNumber(citation);

    val definedTermsList: List[DefinedTerm] = LsParser.parseDefinitions(extractor.extractDefinitionsSection(clensedText))

    val factorsSection: (Int, String) = extractor.extractFactorSection(clensedText)
    val factors: (StandardOfProof, List[(String, String)]) = LsParser.parseFactors(factorsSection._2)

    val factorObjects = factors._2
      .map(f => (factorsSection._1 + f._1,f._2)) // prepend para number to letter
      .map(f =>
        { val relevantDefinitions = definedTermsList.filter(d => f._2.contains(d.getTerm)).toSet
          new ParsedFactor(f._1,f._2,relevantDefinitions)
        }
      )

    val effectiveFromDate: LocalDate = LsParser.parseDateOfEffect(extractor.extractDateOfEffectSection(clensedText))

    val standardOfProof = factors._1
    new ParsedSop(registerId,instrumentNumber,citation,Set(), factorObjects.toSet, effectiveFromDate,standardOfProof)

  }
}
