package au.gov.dva.sopapi.sopref.parsing.implementations.extractors

import au.gov.dva.sopapi.exceptions.SopParserError
import au.gov.dva.sopapi.interfaces.model.ICDCode
import au.gov.dva.sopapi.sopref.parsing.PostAug2015ExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.traits.SoPExtractor

import scala.util.Properties
import scala.util.matching.Regex

class PostAug2015Extractor(cleansedText: String) extends SoPExtractor {

  val sections: List[(Option[Int], String, List[String])] = PostAug2015ExtractorUtilities.getSectionBlocks(cleansedText)

  private def getSection(titleRegex: Regex) = {
    val section = sections.find(s => titleRegex.findFirstMatchIn(s._2).isDefined)
    if (section.isEmpty) throw new SopParserError(s"Cannot find section with title matching regex ${titleRegex.pattern.toString} in text: ${Properties.lineSeparator}$cleansedText")
    val (sectionNumberOpt, sectionTitle, sectionLines) = section.get
    if (sectionNumberOpt.isEmpty) throw new SopParserError("Cannot find section number for section with title: " + sectionTitle)
    (sectionNumberOpt.get, sectionLines.mkString(Properties.lineSeparator))
  }

  override def extractFactorsSection(plainTextSop: String): (Int, String) = getSection("""^Factors that must exist$""".r)

  override def extractDefinitionsSection(plainTextSop: String): String =  {
    val s = getSection("""^Definitions$""".r)
    assert(s._1 == 1)
    s._2
  }

  override def extractDateOfEffectSection(plainTextSop: String): String = getSection("""^Commencement$""".r)._2

  override def extractCitation(plainTextSop: String): String = getSection("""^Name$""".r)._2

  override def extractICDCodes(plainTextSop: String): List[ICDCode] =  {
    PreAugust2015Extractor.extractICDCodes(plainTextSop)
  }

  override def extractAggravationSection(plainTextSop: String): String = getSection("""^Relationship to service$""".r)._2

}
