package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.PostAug2015Extractor
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PostAugust2015Parser
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory

object PostAug2015SoPFactory extends SoPFactory {
  override def create(registerId : String, cleanesdText: String) : SoP = {
    create(registerId,cleanesdText,new PostAug2015Extractor(cleanesdText), PostAugust2015Parser)
  }
}
