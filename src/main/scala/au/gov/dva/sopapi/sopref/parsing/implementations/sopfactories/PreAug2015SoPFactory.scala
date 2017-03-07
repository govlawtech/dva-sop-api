package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.PreAugust2015Extractor
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PreAugust2015Parser
import au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories.OsteoarthritisSoPFactory.create
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory

object PreAug2015SoPFactory extends SoPFactory {
  override def create(registerId : String, cleansedText: String): SoP = {
    create(registerId,cleansedText,PreAugust2015Extractor,PreAugust2015Parser)
  }
}


