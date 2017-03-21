package au.gov.dva.sopapi.sopref.parsing.traits

import com.typesafe.scalalogging.Logger

import scala.util.Properties
import scala.util.matching.Regex


trait GenericTextCleanser extends SoPCleanser {

  private val lineEndRegexPattern = "[\r\n]+"

  val logger = Logger[GenericTextCleanser]

  def removeAuthorisedFootnote(raw: String): String = {

    val asLines = raw.split(lineEndRegexPattern).toList

    val footNoteRegex1 = """(?i)Federal\s+Register\s+of\s+Legislative\s+Instruments(?-i)\s+F[0-9]{4,4}[A-Z0-9]{6,6}(?-i)""".r
    val footNoteRegex2 = """(?i)Authorised\s+Version\s+F[0-9]{4,4}[A-Z0-9]{6,6}\s+registered\s+[0-9]{2,2}/[0-9]{2,2}/[0-9]{4,4}(?-i)""".r

    asLines
      .filter(l => footNoteRegex1.findAllMatchIn(l).isEmpty)
      .filter(l => footNoteRegex2.findFirstMatchIn(l).isEmpty)
      .mkString(Properties.lineSeparator)
  }

  def removeCompilationFootnote(raw: String): String = {

    val asLines = raw.split(lineEndRegexPattern).toList

    val compilationFootNoteRegexLine1 = """(?i)\s*statement of principles concerning .* [0-9]{4,4} [0-9]+\s*$(?-i)""".r
    val compilationFootnoteRegexLine2 = """(?i)\s*compilation no\. [0-9].*$(?-i)""".r


    asLines
      .filter(l => compilationFootNoteRegexLine1.findAllMatchIn(l).isEmpty)
      .filter(l => compilationFootnoteRegexLine2.findFirstMatchIn(l).isEmpty)
      .mkString(Properties.lineSeparator)
  }

  def regexReplace(regex: Regex, target: String, replacement: String = "") = {
    val matches = regex.findAllMatchIn(target);
    val matchCount = matches.size
    if (matchCount > 0)
      regex.replaceAllIn(target, replacement);
    else target;
  }

  def removePageNumberFootNote(raw: String): String = {
    val pageNumberRegex = """(?i)Page\s+[0-9]+\s+of\s+[0-9]+\s+of\s+Instrument\s+No.?\s+[0-9]+\s+of\s+[0-9]{4,4}""".r
    regexReplace(pageNumberRegex, raw)
  }

  private def trimStart(raw: String): String = {
    var startRegex = """^[^a-zA-Z0-9]*""".r
    startRegex.replaceFirstIn(raw, "")
  }

  def removePageGaps(raw: String): String = {
    val pageGapRegex = """(\x{0020}*[\r\n]+){2,}""".r
    regexReplace(pageGapRegex, raw, scala.util.Properties.lineSeparator);
  }

  private def compressSpaces(raw: String): String = {
    val multipleSpacesRegex = """\x{0020}{2,}""".r
    regexReplace(multipleSpacesRegex, raw, " ")
  }

  private def replaceCurlyApostrophe(raw: String): String = {
    val curlyApostrophe = """\u2019|\u0092""".r
    regexReplace(curlyApostrophe, raw, "'")
  }

  // todo: more robust way to do this would be to implement custom PDFTextStripper
  private def reinsertExponents(raw: String): String = {
    val whitelisted = Map(
      """m/s[\r\n]*2""".r -> "m/s\u00B2",
      """W/H[\r\n]*2""".r -> "W/H\u00B2")

    val fixed = whitelisted.foldLeft(raw)((currentText, mapItem) => regexReplace(mapItem._1, currentText, mapItem._2))
    fixed
  }

  private def stripNotes(raw: String): String = {
    val noteRegex = """Note:\s.*[\r\n]+""".r
    regexReplace(noteRegex, raw)
  }



  override def cleanse(rawText: String) = {
    List(rawText)
      .map(trimStart)
      .map(removeCompilationFootnote)
      .map(removeAuthorisedFootnote)
      .map(removePageNumberFootNote)
      .map(stripNotes)
      .map(removePageGaps)
      .map(compressSpaces)
      .map(replaceCurlyApostrophe)
      .map(reinsertExponents)
      .head
  }
}
