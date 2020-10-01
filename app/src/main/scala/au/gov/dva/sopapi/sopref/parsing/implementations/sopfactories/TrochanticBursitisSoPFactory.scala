package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.TrochanticBursitisExtractor
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PreAugust2015Parser
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory

object TrochanticBursitisSoPFactory extends SoPFactory {
  override def create(registerId : String, clensedText: String) : SoP = {
    create(registerId,clensedText,new TrochanticBursitisExtractor(clensedText), PreAugust2015Parser)
  }
}
