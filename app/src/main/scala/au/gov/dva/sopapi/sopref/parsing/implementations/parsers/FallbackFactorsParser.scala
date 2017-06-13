package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, FactorInfoWithoutSubParas}
import au.gov.dva.sopapi.sopref.parsing.traits.MiscRegexes

import scala.util.Properties
import scala.util.matching.Regex

object FallbackFactorsParser extends MiscRegexes {

  private val regexForAnySection = """^\([a-z0-9]+\)""".r
  private val smallRomanRegex = """^\([ixv]++\)""".r

  def parseFactorsSection(factorsSectionLines: List[String], regexForMainPara: Regex): FactorInfo = {
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


  private def splitToParas(factorSectionLines: List[String]): List[List[String]] = {
    if (factorSectionLines.isEmpty) return List()
    val (paraExclFirst, rest) = factorSectionLines.tail.span(line => regexForAnySection.findFirstIn(line).isEmpty)
    factorSectionLines.head +: paraExclFirst :: splitToParas(rest)
  }

  private def lineSetsToClass(linesets: List[List[String]]): List[ParaLines] = {
    linesets.map(l => {
      val legalRef = regexForAnySection.findFirstIn(l.head).get
      ParaLines(legalRef, l)
    })
  }

  private def isChild(prev: MainPara, current: ParaLines): Boolean = {

    // edge cases
    // if prev main factor is '(h)' and ends 'or', this means the (i) para following is a main para, not a small roman
    if (prev.paraLinesParent.legalRef == "(h)" && current.legalRef == "(i)" && prev.getLast.endsWithOr) false
    else if (smallRomanRegex.findFirstIn(prev.getLast.legalRef).isDefined && smallRomanRegex.findFirstIn(current.legalRef).isDefined) true
    else if (smallRomanRegex.findFirstIn(current.legalRef).isDefined && prev.getLast.isSubsHead) true
    else false
  }

  private def groupToMainParas(paraLines: List[ParaLines], acc: List[MainPara]): List[MainPara] = {
    if (paraLines.isEmpty) acc.reverse
    else if (acc.isEmpty) groupToMainParas(paraLines.tail, List(MainPara(paraLines.head, List())))
    else if (isChild(acc.head, paraLines.head)) {
      // add as child to prev head
      groupToMainParas(paraLines.tail, acc.head.withExtraChild(paraLines.head) :: acc.tail)
    }
    else {
      // new main
      groupToMainParas(paraLines.tail, MainPara(paraLines.head, List()) :: acc)
    }
  }

  private def oldStyleSmallLetterLinesToParas(lines: List[String]): List[MainPara] = {
    val paras: List[List[String]] = FallbackFactorsParser.splitToParas(lines)
    val lineSets = FallbackFactorsParser.lineSetsToClass(paras)
    val mainParas = FallbackFactorsParser.groupToMainParas(lineSets, List())
    mainParas
  }

  def oldStyleSmallLetterLinesToFactors(lines: List[String]): List[FactorInfo] = {
    oldStyleSmallLetterLinesToParas(lines).map(mp => new FactorInfoWithoutSubParas(mp.paraLinesParent.legalRef,
      reflowFactorLines(mp.flattenLines)))
  }

  private def reflowFactorLines(factorText: String): String = {
    // remove trailing '; or' or '.'
    // remove para letter at start
    // replace line break in front of 'or'


    def regexReplace(regex: Regex, target: String, replacement: String = "") = {
      val matches = regex.findAllMatchIn(target)
      val matchCount = matches.size
      if (matchCount > 0)
        regex.replaceAllIn(target, replacement)
      else target
    }

    List(factorText)
      .map(regexReplace(""";(\n|\r\n)or$""".r, _))
      .map(regexReplace(""";$""".r,_))
      .map(regexReplace("""; or$""".r, _))
      .map(regexReplace("""^\([a-z0-9]+\)\s?""".r, _))
      .head
  }

  case class ParaLines(legalRef: String, lines: List[String]) {
    def endsWithOr = lines.last.endsWith("or")

    def isSubsHead = lines.last.endsWith(":") || lines.last.endsWith(",")
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
