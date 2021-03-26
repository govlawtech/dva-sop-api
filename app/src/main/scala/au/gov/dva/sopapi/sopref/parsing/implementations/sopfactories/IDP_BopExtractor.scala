package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.{IDP_BopExtractor, TrochanticBursitisExtractor}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PostAugust2015Parser
import au.gov.dva.sopapi.sopref.parsing.traits.SoPFactory


object IDP_BopFactory
  extends SoPFactory {
  override def create(registerId : String, clensedText: String) : SoP = {
    create(registerId,clensedText,new IDP_BopExtractor(clensedText), PostAugust2015Parser)
  }
}
