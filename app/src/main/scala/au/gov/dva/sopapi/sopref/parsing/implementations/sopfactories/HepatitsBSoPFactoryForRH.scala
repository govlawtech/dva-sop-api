package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import java.time.LocalDate

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, Factor, ICDCode, SoP}
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.{BlephartisExtractor, PostAug2015Extractor}
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, ParsedSop}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.{PostAugust2015Parser, PreAugust2015Parser}
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory

object HepatitsBSoPFactoryForRH extends SoPFactory {

  private def factorAllocator(factors: List[Factor]) : (List[Factor],List[Factor]) = {
    if (factors.size != 5) throw new SopParserRuntimeException("Expected five factors only for Hepatitis B.")

    (factors.take(3),factors.drop(2))
  }

  override def create(registerId : String, clensedText: String) : SoP = {
      create(registerId,clensedText,new PostAug2015Extractor(clensedText),PostAugust2015Parser,factorAllocator)
  }

}


