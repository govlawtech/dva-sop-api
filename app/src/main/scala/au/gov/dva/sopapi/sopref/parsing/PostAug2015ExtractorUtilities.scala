package au.gov.dva.sopapi.sopref.parsing

import au.gov.dva.sopapi.sopref.parsing.traits.MiscRegexes

import scala.util.matching.Regex

object PostAug2015ExtractorUtilities extends MiscRegexes {

  def getSections(cleansedSoPText: String): List[List[String]] = {
    val sectionHeaderLineRegex = """^[0-9]+\s[A-Z][a-zA-Z,\s]+$""".r
    val acc = List[List[String]]()
    val lines = cleansedSoPText.split(platformNeutralLineEndingRegex.regex).toList
    divideRecursive(sectionHeaderLineRegex, acc, lines)
  }

  private def divideRecursive(sectionHeaderLineRegex: Regex, acc: List[List[String]], remaining: List[String]): List[List[String]] = {
    if (remaining.isEmpty)
      acc
    else {
      val sectionLinesToNextHeading: List[String] = remaining.head :: remaining.tail.takeWhile(l => sectionHeaderLineRegex.findFirstMatchIn(l).isEmpty)
      divideRecursive(sectionHeaderLineRegex, sectionLinesToNextHeading :: acc, remaining.drop(sectionLinesToNextHeading.size))
    }
  }

  def parseSectionBlock(sectionBlock: List[String]): (Option[Int], String, List[String]) = {
    val lines = sectionBlock
    val titleLine: String = lines.head
    val sectionHeaderLineRegex = """^([0-9]+)\s([A-Za-z\s]+)$""".r
    val m = sectionHeaderLineRegex.findFirstMatchIn(titleLine)
    if (m.isDefined) {
      val sectionNumber = m.get.group(1).toInt
      val title = m.get.group(2).trim
      val bodyText: List[String] = lines.drop(1).map(_.trim)
      return (Some(sectionNumber), title, bodyText)
    }
    else {
      return (None, titleLine, lines.tail)
    }
  }

  def getSectionBlocks(cleansedText: String) : List[(Option[Int],String,List[String])] = {
    getSections(cleansedText)
      .map(parseSectionBlock(_))
  }

}
