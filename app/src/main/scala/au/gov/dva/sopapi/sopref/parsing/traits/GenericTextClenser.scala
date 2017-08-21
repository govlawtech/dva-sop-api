package au.gov.dva.sopapi.sopref.parsing.traits

import com.typesafe.scalalogging.Logger

import scala.util.Properties
import scala.util.matching.Regex


trait GenericTextClenser extends SoPClenser {

  private val lineEndRegexPattern = "(\n|(\r\n))"

  val logger = Logger[GenericTextClenser]


  def regexReplace(regex: Regex, target: String, replacement: String = "") = {
    val matches = regex.findAllMatchIn(target);
    val matchCount = matches.size
    if (matchCount > 0)
      regex.replaceAllIn(target, replacement);
    else target;
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



  override def clense(rawText: String) = {
    List(rawText)
      .map(trimStart)
      .map(removePageGaps)
      .map(compressSpaces)
      .map(replaceCurlyApostrophe)
      .map(reinsertExponents)
      .head
  }
}
