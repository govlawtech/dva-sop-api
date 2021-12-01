package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.SubstituteCommencementDateExtractor
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PostAugust2015Parser
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory

class SubstituteCommencementDateFactory(longFormDate : String) extends SoPFactory {
  override def create(registerId: String, cleansedText: String): SoP = create(registerId,cleansedText, new SubstituteCommencementDateExtractor(cleansedText,longFormDate), PostAugust2015Parser)
}



