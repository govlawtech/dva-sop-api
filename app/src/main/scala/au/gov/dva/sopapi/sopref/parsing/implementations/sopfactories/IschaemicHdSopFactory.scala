package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.{GBSyndromeExtractor, IschaemicHeartDiseaseSyndromeExtractor}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PostAugust2015Parser
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory

object IschaemicHdSopFactory extends SoPFactory {
    override def create(registerId: String, cleansedText: String): SoP = create(registerId,cleansedText, new IschaemicHeartDiseaseSyndromeExtractor(clensedText = cleansedText), PostAugust2015Parser)
}
