package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.RenalStoneRHFactorsParser
import au.gov.dva.sopapi.sopref.parsing.traits.{OldSoPStyleExtractor, SoPFactory}

object RenalStoneRHSoPFactory extends SoPFactory {

    override def create(registerId: String, cleansedText: String): SoP =  {
      create(registerId,cleansedText, new OldSoPStyleExtractor(cleansedText), RenalStoneRHFactorsParser)
    }
}
