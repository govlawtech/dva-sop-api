package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.{PostAugust2015Parser, PreAugust2015Parser}
import au.gov.dva.sopapi.sopref.parsing.traits.{OldSoPStyleExtractor, SoPFactory}

object PreAug2015SoPFactory extends SoPFactory {
  override def create(registerId : String, cleansedText: String): SoP = {
    create(registerId,cleansedText, new OldSoPStyleExtractor(cleansedText),PreAugust2015Parser)
  }
}



