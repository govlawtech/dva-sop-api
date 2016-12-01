package au.gov.dva.sopref.parsing.traits

/**
  * Created by Nick on 1/12/2016.
  */
trait SoPExtractor {
  def extractFactorSection(plainTextSop : String) : (Int,String)
  def extractDefinitionsSection(plainTextSop : String) : String
  def extractCommencementSection(plainTextSop : String) : String
  def extractCitation(plainTextSop : String) : String
}
