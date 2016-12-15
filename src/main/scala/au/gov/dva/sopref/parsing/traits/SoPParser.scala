package au.gov.dva.sopref.parsing.traits

import java.time.LocalDate

import au.gov.dva.interfaces.model._
import au.gov.dva.sopapi.dtos.StandardOfProof

trait SoPParser {
  def parseFactors(factorsSection : String) : (StandardOfProof, List[(String,String)])
  def parseInstrumentNumber(citationSection : String) : InstrumentNumber
  def parseDefinitions(definitionsSection : String) : List[DefinedTerm]
  def parseDateOfEffect(dateOfEffectSection : String) : LocalDate
  def parseStartAndEndAggravationParas(aggravationSection : String) : (String,String)
  def parseCitation(citationSection : String) : String
  def parseConditionNameFromCitation(citation : String) : String
}
















