package au.gov.dva.sopapi.sopref.parsing.traits

import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.interfaces.model.ICDCode
import au.gov.dva.sopapi.sopref.data.sops.BasicICDCode
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities.{getSections, parseSectionBlock}

import scala.util.Properties
import scala.util.matching.Regex

// Pre August 2015 SoPs
class OldSoPStyleExtractor(cleansedText: String) extends SoPExtractor {

  private val sectionHeaderLineRegex = """^([0-9]+)\.\s""".r

  private val allSections: List[(Option[Int], String, List[String])] = getSections(cleansedText, sectionHeaderLineRegex).map(s => parseSectionBlock(s))

  private def getSectionWithTitleMatchingRegexOrThrow(titleRegex: Regex) = {
    val sectionForSpecifiedPara = getSectionWithTitleMatchingRegexOpt(titleRegex)
    if (sectionForSpecifiedPara.isEmpty)
      throw new SopParserRuntimeException("No section found with title matching regex: " + titleRegex.regex)
    if (sectionForSpecifiedPara.get._1.isEmpty)
      throw new SopParserRuntimeException("Could not determine section number using regex: " + sectionHeaderLineRegex.regex)
    (sectionForSpecifiedPara.get._1.get, sectionForSpecifiedPara.get._3)
  }

  private def getSectionWithTitleMatchingRegexOpt(titleRegex: Regex): Option[(Option[Int], String, List[String])] = {
    allSections.find(s => titleRegex.findFirstIn(s._2).nonEmpty)
  }

  // todo:
  // if no 'Factors' section, and only a 'Factors that must be related to service section', it means
  // there is only one factor - there won't be any aggravation section
  // the section to parse for factors is actually the one with title 'Factors that must be related to service'
  override def extractFactorsSection(plainTextSop: String): (Int, String) = {

    val regexForMultipleFactorsSection = """^Factors$""".r
    val regexForSingleFactorsSection = """Factors? that must be related to service$""".r

    val factorsSection = {
      val factorsSectionForMultipleSoPFactors = getSectionWithTitleMatchingRegexOpt(regexForMultipleFactorsSection)
      if (factorsSectionForMultipleSoPFactors.isEmpty) {
        getSectionWithTitleMatchingRegexOrThrow(regexForSingleFactorsSection)
      }
      else getSectionWithTitleMatchingRegexOrThrow(regexForMultipleFactorsSection)
    }

    val (sectionNumber, text): (Int, List[String]) = factorsSection
    (sectionNumber, text.mkString(Properties.lineSeparator))
  }

  override def extractDefinitionsSection(plainTextSop: String): String = {
    val headingRegex = """^Other definitions$""".r
    val definitionsSection = getSectionWithTitleMatchingRegexOrThrow(headingRegex)
    definitionsSection._2.mkString("\n")
  }

  override def extractDateOfEffectSection(plainTextSop: String): String = {
    val doeRegex = """Date of effect""".r
    val doeToEnd = getSectionWithTitleMatchingRegexOrThrow(doeRegex);
    doeToEnd._2.take(1).mkString
  }

  override def extractCitation(plainTextSop: String): String = {
    val citationRegex = "^(Title|Name)$".r
    getSectionWithTitleMatchingRegexOrThrow(citationRegex)._2.mkString(" ")
  }

  override def extractICDCodes(plainTextSop: String): List[ICDCode] = {
    val icdCodeStatementRegex = """attracts ICD-10-AM code((\s|[0-9]|\.|[A-Z]|,|[\r\n]+|or|and)+(?!\.[\r\n]+))""".r
    val firstIcdMatch = icdCodeStatementRegex.findFirstMatchIn(plainTextSop)

    if (firstIcdMatch.isEmpty) {
      List()
    } else {
      val allCodes = firstIcdMatch.get.group(1)
      val individualCodeRegex = """[A-Z]+[0-9]+(\.[0-9]+)?""".r
      val individualsCodes = individualCodeRegex.findAllMatchIn(allCodes)
        .map(regexMatch => new BasicICDCode("ICD-10-AM", regexMatch.matched.trim))
      individualsCodes.toList
    }
  }

  override def extractAggravationSection(plainTextSop: String): Option[String] = {
    val aggravationSectionRegex = """Factors that apply only to material contribution or aggravation""".r
    getSectionWithTitleMatchingRegexOpt(aggravationSectionRegex) match  {
      case Some(s) => Some(s._3.mkString(" "))
      case None => None
    }
  }
}
