package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.interfaces.model.{Factor, SoP}
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.PostAug2015Extractor
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PostAugust2015Parser
import au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories.HepatitsBSoPFactoryForBoP.{create, factorAllocator}
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory

object MalignantNeoplasmOfTheOverySoPFactory extends SoPFactory  {

  private def factorAllocator(factors: List[Factor]) : (List[Factor],List[Factor]) = {


    (factors.take(2),factors.drop(2))
  }

  override def create(registerId : String, clensedText: String) : SoP = {
    create(registerId,clensedText,new PostAug2015Extractor(clensedText),PostAugust2015Parser,factorAllocator)
  }
}
