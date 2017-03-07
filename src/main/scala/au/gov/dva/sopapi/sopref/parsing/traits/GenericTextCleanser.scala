package au.gov.dva.sopapi.sopref.parsing.traits

import com.typesafe.scalalogging.Logger

import scala.util.matching.Regex


trait GenericTextCleanser extends SoPCleanser {

  val logger = Logger[GenericTextCleanser]

  def removeAuthorisedFootnote(raw: String): String = {
    val footNoteRegex1 = """(?i)Federal\s+Register\s+of\s+Legislative\s+Instruments(?-i)\s+F[0-9]{4,4}[A-Z0-9]{6,6}(?-i)""".r
    val footNoteRegex2 = """(?i)Authorised\s+Version\s+F[0-9]{4,4}[A-Z0-9]{6,6}\s+registered\s+[0-9]{2,2}/[0-9]{2,2}/[0-9]{4,4}(?-i)""".r
    List(raw).map(regexReplace(footNoteRegex1, _))
      .map(regexReplace(footNoteRegex2, _))
      .head
  }

  private def regexReplace(regex: Regex, target: String, replacement: String = "") = {
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

  private def removePageGaps(raw: String): String = {
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
      """m/s[\r\n]+2""".r -> "m/s\u00B2",
      """W/H[\r\n]+2""".r -> "W/H\u00B2")

    val fixed = whitelisted.foldLeft(raw)((currentText,mapItem) => regexReplace(mapItem._1,currentText,mapItem._2))
    fixed

  }

  override def cleanse(rawText: String) = {
    List(rawText)
      .map(trimStart)
      .map(removeAuthorisedFootnote)
      .map(removePageNumberFootNote)
      .map(removePageGaps)
      .map(compressSpaces)
      .map(replaceCurlyApostrophe)
      .map(reinsertExponents)
      .head
  }
}
