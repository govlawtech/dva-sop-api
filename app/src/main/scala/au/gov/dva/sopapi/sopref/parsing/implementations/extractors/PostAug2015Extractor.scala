package au.gov.dva.sopapi.sopref.parsing.implementations.extractors

import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.interfaces.model.ICDCode
import au.gov.dva.sopapi.sopref.parsing.PostAug2015ExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.traits.{MiscRegexes, OldSoPStyleExtractor, SoPExtractor}
import com.typesafe.scalalogging.Logger

import scala.util.Properties
import scala.util.matching.Regex

class PostAug2015Extractor(cleansedText: String) extends SoPExtractor with MiscRegexes {

  private val logger = Logger("dvasopapi.newsopstyleextractor")

    val sections: List[(Option[Int], String, List[String])] = PostAug2015ExtractorUtilities.getSectionBlocks(cleansedText)

  private def stripLines(startRegex: Regex, endRegex: Regex, toProcess : List[String], processed: List[String], inSection: Boolean, linesRemoved: Int) : List[String] = {
    if (toProcess.isEmpty) return processed.reverse
    if (!inSection)
    {
      val lineIsNote = startRegex.findFirstMatchIn(toProcess.head).isDefined
      if (lineIsNote)
      {
        stripLines(startRegex,endRegex,toProcess.tail,processed,inSection = true,linesRemoved + 1)
      }

      else stripLines(startRegex,endRegex,toProcess.tail,toProcess.head +: processed,inSection = false,0)
    }
    else {
      val lineIsNextAfterNoteEnds = endRegex.findFirstMatchIn(toProcess.head).isDefined
      if (lineIsNextAfterNoteEnds) stripLines(startRegex,endRegex,toProcess.tail, toProcess.head +: processed,inSection = false,0)
      else stripLines(startRegex,endRegex,toProcess.tail,processed,inSection = true, linesRemoved + 1)
    }

  }
 def stripNotes(lines: List[String]): List[String] = {

    val noteStartRegex = """^Note:""".r
    val noteEndRegex = """^[\(0-9]""".r

    stripLines(noteStartRegex,noteEndRegex,lines,List(),inSection = false,0)
  }

  private def getSection(titleRegex : Regex) : Option[(Int,String)] = {
    val section = sections.find(s => titleRegex.findFirstMatchIn(s._2).isDefined)
    if (section.isEmpty) return None
    val (sectionNumberOpt, sectionTitle, sectionLines) = section.get
    if (sectionNumberOpt.isEmpty) throw new SopParserRuntimeException("Cannot find section number for section with title: " + sectionTitle)
    else return Some(sectionNumberOpt.get, sectionLines.mkString(Properties.lineSeparator))
  }

  private def getSectionOrThrow(titleRegex: Regex) = {
    val section = sections.find(s => titleRegex.findFirstMatchIn(s._2).isDefined)
    if (section.isEmpty) throw new SopParserRuntimeException(s"Cannot find section with title matching regex ${titleRegex.pattern.toString} in text: ${Properties.lineSeparator}$cleansedText")
    val (sectionNumberOpt, sectionTitle, sectionLines) = section.get
    if (sectionNumberOpt.isEmpty) throw new SopParserRuntimeException("Cannot find section number for section with title: " + sectionTitle)
    (sectionNumberOpt.get, sectionLines.mkString(Properties.lineSeparator))
  }




  override def extractFactorsSection(plainTextSop: String): (Int, String) =  {
    val section =  getSectionOrThrow("""^Factors? that must exist$""".r)
    val sectionLines = section._2.split(platformNeutralLineEndingRegex.regex).toList
    val withNotesRemoved = stripNotes(sectionLines)
    (section._1,withNotesRemoved.mkString(Properties.lineSeparator ))
  }

  override def extractDefinitionsSection(plainTextSop: String): String =  {
    val s = getSectionOrThrow("""^Definitions$""".r)
    if (s._1 != 1) throw new SopParserRuntimeException("Expected number of definition section to be 1.")
    s._2
  }

  override def extractDateOfEffectSection(plainTextSop: String): String = getSectionOrThrow("""^Commencement$""".r)._2

  override def extractCitation(plainTextSop: String): String = getSectionOrThrow("""(^Name$|^Title$)""".r)._2

  override def extractICDCodes(plainTextSop: String): List[ICDCode] =  {
     new OldSoPStyleExtractor(plainTextSop).extractICDCodes(plainTextSop)
  }

  override def extractAggravationSection(plainTextSop: String): Option[String] =  {
    getSection("""^Relationship to service$""".r) match {
      case Some(s) =>   Some(s._2)
      case None => None
    }
  }

}


