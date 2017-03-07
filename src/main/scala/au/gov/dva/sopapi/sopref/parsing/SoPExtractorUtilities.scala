package au.gov.dva.sopapi.sopref.parsing

import au.gov.dva.sopapi.exceptions.SopParserError

import scala.collection.immutable.{IndexedSeq, Seq}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Properties
import scala.util.matching.Regex


object SoPExtractorUtilities {

  def getSections(cleansedSoPText: String, sectionHeaderLineRegex: Regex): List[List[String]] = {
    val acc = List[List[String]]();
    val lines = cleansedSoPText.split("[\r\n]+").toList // todo: change to platform indep line sep
    divideRecursive(List.empty, sectionHeaderLineRegex, acc, lines)
  }

  private def divideRecursive(nextSectionTitle: List[String], sectionHeaderLineRegex: Regex, acc: List[List[String]], remaining: List[String]): List[List[String]] = {
    if (remaining.isEmpty)
      acc
    else {
      val sectionLinesWithTitleForNextHeading: List[String] = remaining.head :: remaining.tail.takeWhile(l => sectionHeaderLineRegex.findFirstMatchIn(l).isEmpty)
      val headerForNextSection = sectionLinesWithTitleForNextHeading.takeRight(1).map(i => i.trim)
      val sectionLinesWithoutTitleForNextSection = sectionLinesWithTitleForNextHeading.dropRight(1)
      val sectionLines = (nextSectionTitle ++ sectionLinesWithoutTitleForNextSection).map(l => l.trim)
      divideRecursive(headerForNextSection, sectionHeaderLineRegex, sectionLines :: acc, remaining.drop(sectionLinesWithTitleForNextHeading.size))
    }
  }

  def parseSectionBlock(sectionBlock: List[String]): (Option[Int], String, List[String]) = {
    val lines = sectionBlock
    val title: String = lines.head
    val numberedLine: String = lines.tail(0)
    val sectionHeaderLineRegex = """^([0-9]+)\.\s""".r
    val m = sectionHeaderLineRegex.findFirstMatchIn(numberedLine)
    if (m.isDefined) {
      val sectionNumber = m.get.group(1).toInt
      val bodyTextWithoutSectionNumber: List[String] = List(sectionHeaderLineRegex.replaceFirstIn(numberedLine, "")) ++ lines.tail.drop(1)
      return (Some(sectionNumber), title, bodyTextWithoutSectionNumber)
    }
    else {
      return (None, title, lines.tail)
    }
  }


  def getSection(cleansedSopText: String, paragraphLineRegex: Regex): (Int, List[String]) = {
    val sectionHeaderLineRegex = """^([0-9]+)\.\s""".r
    val allSections: List[(Option[Int], String, List[String])] = getSections(cleansedSopText, sectionHeaderLineRegex).map(s => parseSectionBlock(s))

    val sectionForSpecifiedPara = allSections.find(s => paragraphLineRegex.findFirstIn(s._2).nonEmpty)
    if (sectionForSpecifiedPara.isEmpty)
      throw new SopParserError("No section found with title matching regex: " + paragraphLineRegex.regex)

    if (sectionForSpecifiedPara.get._1.isEmpty)
      throw new SopParserError("Could not determine section number using regex: " + sectionHeaderLineRegex.regex)

    (sectionForSpecifiedPara.get._1.get, sectionForSpecifiedPara.get._3)
  }


  def getMainParaLetterSequence = {
    val aToz = 'a' to 'z'
    val doubled = aToz.map(l => s"$l$l")
    val combined = aToz ++ doubled
    combined.map(i => "(" + i + ")")
  }

  def getSubParaLetterSequence = {
    List("i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x", "xi", "xii", "xiii")
      .map(i => ("(" + i + ")"))
  }

  def splitFactorsSectionToHeaderAndRest(factorsSection: List[String]): (String, List[String]) = {
    val (headerLines, rest) = factorsSection.span(l => !l.startsWith("("))
    if (headerLines.isEmpty || rest.isEmpty) throw new SopParserError(s"Cannot split this factors section to head and then the rest: ${factorsSection.mkString(Properties.lineSeparator)}")
    (headerLines.mkString(" "), rest)
  }

  def splitFactorToHeaderAndRest(singleFactor: List[String]): (String, List[String]) = {
    if (singleFactor.size == 1)
      return (singleFactor(0), List.empty)
    else {

      val headerLines = singleFactor.head :: singleFactor.tail.takeWhile(l => !l.startsWith("("))
      val rest = singleFactor.drop(headerLines.size)
      return (headerLines.mkString(" "), rest)
    }

  }

  def splitOutTailIfAny(lastSubParaWithLineBreaks: String): (String, Option[String]) = {
    val asLines = lastSubParaWithLineBreaks.split("[\r\n]+").toList
    val reversed = asLines.reverse
    val tail = reversed.takeWhile(l => !l.endsWith(";")).reverse
    if (tail.size < asLines.size) {
      val partWithoutTail = asLines.take(asLines.size - tail.size)
      return (partWithoutTail.mkString(Properties.lineSeparator), Some(tail.mkString(Properties.lineSeparator)))
    }
    else return (lastSubParaWithLineBreaks, None)
  }

  def splitFactorsSectionByFactor(factorsSectionExcludingHead: List[String]): List[List[String]] = {
    val lettersSequence = getMainParaLetterSequence.toList
    divideSectionRecursively(lettersSequence, 1, List.empty, factorsSectionExcludingHead)
  }

  def divideSectionRecursively(lettersSequence: List[String], nextLetterIndex: Int, acc: List[List[String]], remainingLines: List[String])
  : List[List[String]] = {
    if (remainingLines.isEmpty)
      acc
    else {
      val (factorLines, rest) = partition(lettersSequence, nextLetterIndex, remainingLines)
      divideSectionRecursively(lettersSequence, nextLetterIndex + 1, acc :+ factorLines, rest)
    }
  }

  def splitFactorToSubFactors(factorLines: List[String]): List[List[String]] = {
    val romanNumeralsSequence = getSubParaLetterSequence
    divideSectionRecursively(romanNumeralsSequence, 1, List.empty, factorLines)
  }

  private def partition(letterSequence: List[String], nextLetterIndex: Int, remainingLines: List[String]): (List[String], List[String]) = {


    // edge case of (i) following (h)
    if (letterSequence(nextLetterIndex - 1) == "(h)" && letterSequence(nextLetterIndex) == "(i)") {
      if (remainingLines.head.endsWith(",")) {
        return splitWithSkip(remainingLines,2, lineStartsWithLetter("(i)"));
      }
    }

    // edge case of (ii) following (hh)
    if (letterSequence(nextLetterIndex - 1) == "(hh)" && letterSequence(nextLetterIndex) == "(ii)") {
      if (remainingLines.head.endsWith(",")) {
        return splitWithSkip(remainingLines,2, lineStartsWithLetter("(ii)"));
      }
    }


    val nextLetter = letterSequence(nextLetterIndex)
    remainingLines.span(l => !l.startsWith(nextLetter))

  }

  def splitWithSkip(lines: List[String], numberOfTimes: Int, test: String => Boolean): (List[String],List[String]) = {
    val firstPart = takeUntilTestPassedNTimes(lines,numberOfTimes,test)
    (firstPart,lines.drop(firstPart.size))
  }


  def takeUntilTestPassedNTimes(lines: List[String], numberOfTimes: Int, test: String => Boolean): List[String] = {
    def takeRecursive(remaining: List[String], acc: List[String], timesPassed : Int, maxTimes: Int, test: String => Boolean): List[String] = {
      if (remaining.isEmpty) return acc
      else {
        val passOnCurrent = if (test(remaining.head)) 1 else 0
        if (passOnCurrent + timesPassed == maxTimes) return acc
        else takeRecursive(remaining.tail, acc :+ remaining.head, timesPassed + passOnCurrent, maxTimes, test)
      }
    }
    takeRecursive(lines,List(),0,numberOfTimes,test)
  }



  private def lineStartsWithLetter(letter: String) (line: String) = {
    val letterFollowedBySpace = letter + " ";
    line.startsWith(letterFollowedBySpace)
  }

}
