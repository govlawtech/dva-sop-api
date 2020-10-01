package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.BlephartisExtractor
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.{PostAugust2015Parser, PreAugust2015Parser}
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory

object BlephartisSoPFactory extends SoPFactory {
  override def create(registerId : String, clensedText: String) : SoP = {
    create(registerId,clensedText,new BlephartisExtractor(clensedText), PreAugust2015Parser)
  }
}


