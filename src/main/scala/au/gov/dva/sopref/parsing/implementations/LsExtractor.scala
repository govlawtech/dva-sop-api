package au.gov.dva.sopref.parsing.implementations

import au.gov.dva.sopref.parsing.SoPExtractors
import au.gov.dva.sopref.parsing.traits.SoPExtractor

class LsExtractor extends SoPExtractor {
  override def extractFactorSection(plainTextSop: String): (Int,String) = {
    val headingRegex = """^Factors$""".r;
    val factorsSection = SoPExtractors.getSection(plainTextSop,headingRegex)
    (factorsSection._1,factorsSection._2.mkString(" "))
  }
  

  override def extractDefinitionsSection(plainTextSop: String): String = null

  override def extractCommencementSection(plainTextSop: String): String = null

  override def extractCitation(plainTextSop: String): String = null
}
