package au.gov.dva.sopref.parsing.traits

import java.time.LocalDate

import au.gov.dva.sopref.interfaces.model._

trait SoPParser {
  def parseFactors(factorsSection : String) : (StandardOfProof, List[(String,String)])
  def parseInstrumentNumber(citationSection : String) : InstrumentNumber
  def parseDefinitions(definitionsSection : String) : List[DefinedTerm]
  def parseDateOfEffect(dateOfEffectSection : String) : LocalDate
}
















