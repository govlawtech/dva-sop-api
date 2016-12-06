package au.gov.dva.sopref.parsing.traits

import au.gov.dva.sopref.interfaces.model.{ICDCode, InstrumentNumber}

trait SoPExtractor {
  def extractFactorSection(plainTextSop : String) : (Int,String)
  def extractDefinitionsSection(plainTextSop : String) : String
  def extractDateOfEffectSection(plainTextSop : String) : String
  def extractCitation(plainTextSop : String) : String
  def extractICDCodes(plainTextSop : String) : List[ICDCode]
}
