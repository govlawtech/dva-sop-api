package au.gov.dva.sopapi.sopref.parsing.traits

import java.time.LocalDate

import au.gov.dva.sopapi.interfaces.model._
import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.sopref.parsing.implementations.model.FactorInfo

trait SoPParser {
  def parseFactors(factorsSection : String) : (StandardOfProof, List[FactorInfo])
  def parseInstrumentNumber(citationSection : String) : InstrumentNumber
  def parseDefinitions(definitionsSection : String) : List[DefinedTerm]
  def parseDateOfEffect(dateOfEffectSection : String) : LocalDate
  def parseAggravationPara(aggravationSection: String): (String, String)
  def parseStartAndEndAggravationParas(aggravationSection : String) : (String,String)
  def parseCitation(citationSection : String) : String
  def parseConditionNameFromCitation(citation : String) : String
}
















