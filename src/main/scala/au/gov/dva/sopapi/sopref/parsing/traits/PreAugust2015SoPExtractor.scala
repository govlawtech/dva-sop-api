package au.gov.dva.sopapi.sopref.parsing.traits

import au.gov.dva.sopapi.interfaces.model.ICDCode
import au.gov.dva.sopapi.sopref.data.sops.BasicICDCode
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities

import scala.util.Properties

trait PreAugust2015SoPExtractor extends SoPExtractor {

  override def extractFactorsSection(plainTextSop: String): (Int, String) = {
    val headingRegex = """^Factors$""".r;
    val(sectionNumber,text): (Int, List[String]) = SoPExtractorUtilities.getSection(plainTextSop,headingRegex)
    (sectionNumber,text.mkString(Properties.lineSeparator))
  }

  override def extractDefinitionsSection(plainTextSop: String): String = {
    val headingRegex = """^Other definitions$""".r
    val definitionsSection = SoPExtractorUtilities.getSection(plainTextSop,headingRegex);
    definitionsSection._2.mkString("\n")
  }

  override def extractDateOfEffectSection(plainTextSop: String): String =  {
    val doeRegex = """Date of effect""".r
    val doeToEnd = SoPExtractorUtilities.getSection(plainTextSop, doeRegex);
    doeToEnd._2.take(1).mkString
  }

  override def extractCitation(plainTextSop: String): String = {
    val citationRegex = "^Title$".r
    SoPExtractorUtilities.getSection(plainTextSop,citationRegex)._2.mkString(" ")
  }

  override def extractICDCodes(plainTextSop: String): List[ICDCode] = {
    val icdCodeStatementRegex = """attracts ICD-10-AM code((\s|[0-9]|\.|[A-Z]|,|[\r\n]+|or|and)+(?!\.[\r\n]+))""".r
    val firstIcdMatch = icdCodeStatementRegex.findFirstMatchIn(plainTextSop)

    if (firstIcdMatch.isEmpty) {
      Nil
    } else {
      val allCodes = firstIcdMatch.get.group(1)
      val individualCodeRegex = """[A-Z]+[0-9]+(\.[0-9]+)?""".r
      val individualsCodes = individualCodeRegex.findAllMatchIn(allCodes)
        .map(regexMatch => new BasicICDCode("ICD-10-AM",regexMatch.matched.trim))
      individualsCodes.toList
    }
  }

  override def extractAggravationSection(plainTextSop: String): String = {
    val aggravationSectionRegex = """Factors that apply only to material contribution or aggravation""".r
    SoPExtractorUtilities.getSection(plainTextSop,aggravationSectionRegex)._2.mkString(" ")
  }

}
