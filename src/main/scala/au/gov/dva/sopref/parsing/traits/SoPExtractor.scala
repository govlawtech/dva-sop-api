package au.gov.dva.sopref.parsing.traits

trait SoPExtractor {
  def extractFactorSection(plainTextSop : String) : (Int,String)
  def extractDefinitionsSection(plainTextSop : String) : String
  def extractDateOfEffectSection(plainTextSop : String) : String
  def extractCitation(plainTextSop : String) : String
}
