package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, FactorInfoForFactorSectionWithOnlyOneFactor, FactorInfoWithoutSubParas}
import au.gov.dva.sopapi.sopref.parsing.traits.MiscRegexes
import org.apache.poi.hslf.record.MainMaster

import scala.util.Properties
import scala.util.matching.Regex

object FactorsParser extends MiscRegexes {

  private val regexForAnySection = """^\([a-z0-9]+\)(\s+\([xiv]+\))?""".r
  private val smallRomanRegex = """^\([ixv]+\)""".r

  private val singleFactorHeadPivotText = """relevant service is""".r

  def parseFactorsSectionWithSingleFactor(factorSectionLines: List[String]): FactorInfo = {
    if (!factorSectionLines.head.startsWith("The factor that must")) throw new SopParserRuntimeException("Expecting text that defines a single factor as part of the section head, got: " + factorSectionLines.mkString(" "))

    val aroundPivot: Array[String] = factorSectionLines.mkString(" ").split("relevant service is");

    if (aroundPivot.size != 2) throw new SopParserRuntimeException("Cannot find the text which indicates start of factor in this factor section: " + factorSectionLines.mkString(" "))

    new FactorInfoForFactorSectionWithOnlyOneFactor(aroundPivot(1).trim)
  }

  def parseNewStyleFactorsSectionWithMultipleFactors(factorsSectionLines: List[String], regexForMainPara: Regex): FactorInfo = {
    val regexForSection = (regexForMainPara.regex + """([.\s\r\n]*)$""").r

    val asSingleStringWithLineBreaks = factorsSectionLines.mkString(Properties.lineSeparator)

    val m = regexForMainPara.findFirstMatchIn(asSingleStringWithLineBreaks)
    if (m.isDefined) {
      val para = m.get.toString()
      val rest = asSingleStringWithLineBreaks.drop(para.size).mkString.trim

      return new FactorInfoWithoutSubParas(para, rest)
    }
    else {
      throw new SopParserRuntimeException(s"Cannot split the following factors section to para and rest using regex $regexForMainPara: $asSingleStringWithLineBreaks")
    }
  }

  def extractFactorFromFactorSectionHead(factorSectionHead: List[String]): FactorInfo = {
    val flattened = factorSectionHead.mkString(" ")
    assert(flattened.startsWith("The factor that must"))
    val words = flattened.split(' ')
    val (afterLastIs, rest) = words.reverse.span(i => i != "is")
    new FactorInfoWithoutSubParas("", afterLastIs.reverse.mkString(" ").stripSuffix("."))
  }


  def splitToParas(factorSectionLines: List[String], paraRegex : Regex): List[List[String]] = {
    if (factorSectionLines.isEmpty) return List()
    val (paraExclFirst, rest) = factorSectionLines.tail.span(line => paraRegex.findFirstIn(line).isEmpty)
    factorSectionLines.head +: paraExclFirst :: splitToParas(rest, paraRegex)
  }

   def lineSetsToClass(linesets: List[List[String]], regexForSectionStart: Regex): List[ParaLines] = {
    linesets.map(l => {
      val legalRef = regexForSectionStart.findFirstIn(l.head).get
      ParaLines(legalRef, l)
    })
  }

  private def isChild(prev: MainPara, current: ParaLines) = {

    // edge cases
    // if prev main factor is '(h)' and ends 'or', this means the (i) para following is a main para, not a small roman
    if (prev.paraLinesParent.legalRef == "(h)" && current.legalRef == "(i)" && prev.getLast.endsWithOr) false
    // if last child was a small roman and the current is small roman, then it is a following sibling child
    else if (smallRomanRegex.findFirstIn(prev.getLast.legalRef).isDefined && smallRomanRegex.findFirstIn(current.legalRef).isDefined) true
    // first child after main para
    else if (smallRomanRegex.findFirstIn(current.legalRef).isDefined && prev.getLast.isSubsHead) true

    else false
  }

  def groupToMainParas(paraLines: List[ParaLines], acc: List[MainPara], paraLinesShouldBeChildrenAccordingToCustomRule : (MainPara, ParaLines) => Boolean): List[MainPara] = {
    if (paraLines.isEmpty) acc.reverse
    else if (acc.isEmpty) groupToMainParas(paraLines.tail, List(MainPara(paraLines.head, List())),paraLinesShouldBeChildrenAccordingToCustomRule) // first para will always be main para
    else if (isChild(acc.head, paraLines.head) || paraLinesShouldBeChildrenAccordingToCustomRule(acc.head,paraLines.head)) {
      // add as child to prev head
      groupToMainParas(paraLines.tail, acc.head.withExtraChild(paraLines.head) :: acc.tail,paraLinesShouldBeChildrenAccordingToCustomRule)
    }
    else {
      // new main
      groupToMainParas(paraLines.tail, MainPara(paraLines.head, List()) :: acc,paraLinesShouldBeChildrenAccordingToCustomRule)
    }
  }

  private def oldStyleSmallLetterLinesToParas(lines: List[String], paraLinesShouldBeChildrenAccordingToCustomRule : (MainPara, ParaLines) => Boolean): List[MainPara] = {
    val paras: List[List[String]] = FactorsParser.splitToParas(lines,regexForAnySection)
    val lineSets = FactorsParser.lineSetsToClass(paras,regexForAnySection)
    val mainParas = FactorsParser.groupToMainParas(lineSets, List(), paraLinesShouldBeChildrenAccordingToCustomRule)
    mainParas
  }

  def oldStyleSmallLetterLinesToFactors(lines: List[String], paraLinesShouldBeChildrenAccordingToCustomRule : (MainPara, ParaLines) => Boolean): List[FactorInfo] = {

    val groupedToSetsOfParagraphLines = oldStyleSmallLetterLinesToParas(lines, paraLinesShouldBeChildrenAccordingToCustomRule)


     return groupedToSetsOfParagraphLines.map(mp => new FactorInfoWithoutSubParas(mp.paraLinesParent.legalRef,
      reflowFactorLines(mp.flattenLines)))
  }

  private def reflowFactorLines(factorText: String): String = {
    val knownOkEndings = List("rheumatoid arthritis", "toxic maculopathy") // there are typos in these SoPs: omitted punctuation

    if (!factorText.endsWith("or")
      && !factorText.endsWith(".")
      && !factorText.endsWith(";")
      && !knownOkEndings.exists(factorText.endsWith(_))
    ) throw new SopParserRuntimeException("Suspicious factor text: does not end with 'or' or punctuation " + factorText)

    def regexReplace(regex: Regex, target: String, replacement: String = "") = {
      val matches = regex.findAllMatchIn(target)
      val matchCount = matches.size
      if (matchCount > 0)
        regex.replaceAllIn(target, replacement)
      else target
    }


    List(factorText)
      .map(regexReplace(""";(\n|\r\n)or""".r, _))
      .map(_.stripSuffix("."))
      .map(_.stripSuffix(";"))
      .map(_.stripSuffix("; or"))
      .map(regexReplace("""^\([a-z0-9]+\)\s?""".r, _))
      .head
  }

  case class ParaLines(legalRef: String, lines: List[String]) {
    def endsWithOr = lines.last.endsWith("or")
    def isSubsHead = lines.last.endsWith(":") || lines.last.endsWith(",") || lines.last.endsWith(", and")
  }

  case class MainPara(paraLinesParent: ParaLines, children: List[ParaLines]) {
    def getLast = {
      if (children.nonEmpty) children.last
      else paraLinesParent
    }

    def withExtraChild(c: ParaLines) = {
      MainPara(paraLinesParent, children :+ c)
    }

    def flattenLines: String = (paraLinesParent.lines ++ children.flatMap(c => c.lines)).mkString(Properties.lineSeparator)
  }

}
