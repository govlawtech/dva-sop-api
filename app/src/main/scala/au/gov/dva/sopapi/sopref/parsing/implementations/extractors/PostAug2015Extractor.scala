package au.gov.dva.sopapi.sopref.parsing.implementations.extractors

import au.gov.dva.sopapi.exceptions.SopParserError
import au.gov.dva.sopapi.interfaces.model.ICDCode
import au.gov.dva.sopapi.sopref.parsing.PostAug2015ExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.traits.{OldSoPStyleExtractor, SoPExtractor}
import com.typesafe.scalalogging.Logger

import scala.util.Properties
import scala.util.matching.Regex

class PostAug2015Extractor(cleansedText: String) extends SoPExtractor {

  private val logger = Logger("dvasopapi.oldsopstyleextractor")

  val sections: List[(Option[Int], String, List[String])] = PostAug2015ExtractorUtilities.getSectionBlocks(cleansedText)

  private def stripLines(startRegex: Regex, endRegex: Regex, maxLines: Int, toProcess : List[String], processed: List[String], inSection: Boolean, linesRemoved: Int) : List[String] = {
    if (toProcess.isEmpty) return processed.reverse
    if (!inSection)
    {
      val lineIsNote = startRegex.findFirstMatchIn(toProcess.head).isDefined
      if (lineIsNote)
      {
        println("Removed note line: " + toProcess.head)
        stripLines(startRegex,endRegex,maxLines,toProcess.tail,processed,inSection = true,linesRemoved + 1)
      }

      else stripLines(startRegex,endRegex,maxLines,toProcess.tail,toProcess.head +: processed,inSection = false,0)
    }
    else {

      assert(linesRemoved < maxLines)
      val lineIsNextAfterNoteEnds = endRegex.findFirstMatchIn(toProcess.head).isDefined
      if (lineIsNextAfterNoteEnds) stripLines(startRegex,endRegex,maxLines,toProcess.tail, toProcess.head +: processed,inSection = false,0)
      else stripLines(startRegex,endRegex,maxLines,toProcess.tail,processed,inSection = true, linesRemoved + 1)
    }

  }
 def stripNotes(lines: List[String]): List[String] = {

    val noteStartRegex = """^Note:""".r
    val noteEndRegex = """^[\(0-9]""".r

    stripLines(noteStartRegex,noteEndRegex,4,lines,List(),inSection = false,0)
  }

  private def getSection(titleRegex: Regex) = {
    val section = sections.find(s => titleRegex.findFirstMatchIn(s._2).isDefined)
    if (section.isEmpty) throw new SopParserError(s"Cannot find section with title matching regex ${titleRegex.pattern.toString} in text: ${Properties.lineSeparator}$cleansedText")
    val (sectionNumberOpt, sectionTitle, sectionLines) = section.get
    if (sectionNumberOpt.isEmpty) throw new SopParserError("Cannot find section number for section with title: " + sectionTitle)
    (sectionNumberOpt.get, sectionLines.mkString(Properties.lineSeparator))
  }

  override def extractFactorsSection(plainTextSop: String): (Int, String) =  {
    val section =  getSection("""^Factors that must exist$""".r)
    val sectionLines = section._2.split(platformNeutralLineEndingRegex.regex).toList
    val withNotesRemoved = stripNotes(sectionLines)
    (section._1,withNotesRemoved.mkString(Properties.lineSeparator ))
  }

  override def extractDefinitionsSection(plainTextSop: String): String =  {
    val s = getSection("""^Definitions$""".r)
    assert(s._1 == 1)
    s._2
  }

  override def extractDateOfEffectSection(plainTextSop: String): String = getSection("""^Commencement$""".r)._2

  override def extractCitation(plainTextSop: String): String = getSection("""(^Name$|^Title$)""".r)._2

  override def extractICDCodes(plainTextSop: String): List[ICDCode] =  {
     new OldSoPStyleExtractor(plainTextSop).extractICDCodes(plainTextSop)
  }

  override def extractAggravationSection(plainTextSop: String): String = getSection("""^Relationship to service$""".r)._2

}
