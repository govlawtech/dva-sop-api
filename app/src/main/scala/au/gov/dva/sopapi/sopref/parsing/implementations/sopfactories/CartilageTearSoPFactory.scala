package au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories

import java.time.LocalDate

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, ICDCode, SoP}
import au.gov.dva.sopapi.sopref.parsing.implementations.model.ParsedSop
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PreAugust2015Parser
import au.gov.dva.sopapi.sopref.parsing.traits.{OldSoPStyleExtractor, SoPFactory}

object CartilageTearSoPFactory extends SoPFactory{
  override def create(registerId : String,  cleansedText: String): SoP = {
    create(registerId,cleansedText,new OldSoPStyleExtractor(cleansedText),PreAugust2015Parser)
  }

}

