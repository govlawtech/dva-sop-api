package au.gov.dva.sopapi.sopref.parsing.traits

import au.gov.dva.sopapi.interfaces.model.{ICDCode, InstrumentNumber}

trait SoPExtractor {
  def extractFactorsSection(plainTextSop : String) : (Int,String)
  def extractDefinitionsSection(plainTextSop : String) : String
  def extractDateOfEffectSection(plainTextSop : String) : String
  def extractCitation(plainTextSop : String) : String
  def extractICDCodes(plainTextSop : String) : List[ICDCode]
  def extractAggravationSection(plainTextSop : String) : String

}
