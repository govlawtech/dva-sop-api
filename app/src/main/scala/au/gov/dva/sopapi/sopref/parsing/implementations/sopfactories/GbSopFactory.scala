package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.GBSyndromeExtractor
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PostAugust2015Parser
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory

object GbSopFactory extends SoPFactory {
  override def create(registerId: String, cleansedText: String): SoP = create(registerId,cleansedText, new GBSyndromeExtractor(clensedText = cleansedText), PostAugust2015Parser)
}
