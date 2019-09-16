package au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors

import au.gov.dva.sopapi.interfaces.model.Factor
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, SubFactorInfo}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.FactorsParser.{MainPara, ParaLines}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.{FactorsParser, PreAugust2015Parser}
import au.gov.dva.sopapi.sopref.parsing.traits.{MiscRegexes, SubFactorParser}

import scala.collection.immutable
import scala.util.Properties
import scala.util.matching.Regex



class NewStyleSubFactorParser extends SubFactorParser with MiscRegexes {

  val paraStartRegex = """\([a-z]+\)""".r

  override def divideToSubfactors(factor: FactorInfo): List[SubFactorInfo] = {

    val lines = factor.getText.split(platformNeutralLineEndingRegex.regex).toList
    if (lines.size > 1) {
      val paras: List[List[String]] = FactorsParser.splitToParas(lines, paraStartRegex)
      val asClasses = FactorsParser.lineSetsToClass(paras.tail, paraStartRegex)

      val grouped: List[FactorsParser.MainPara] = FactorsParser.groupToMainParas(asClasses,List(), (_, _) => false)

      val subFactorInfos = grouped.map(g => SubFactorInfo(g.paraLinesParent.legalRef, stripTrailingPunctuation(stripLeadingPara(g.flattenLines,paraStartRegex))))
      subFactorInfos

    }
    else {
      List()
    }
  }



  protected def stripTrailingPunctuation(text : String) : String = {
    text.replaceFirst("((; or)|(;))$", "")
  }

  protected def stripLeadingPara(text: String, regexForLeadingPara : Regex) : String = {
    val leadingParaMatch = regexForLeadingPara.findFirstMatchIn(text)
    if (leadingParaMatch.isDefined)
    {
      val textWithParaStripped = text.drop(leadingParaMatch.get.matched.length).trim
      return textWithParaStripped
    }
    text
  }

  val conditionVariantRegexMatch = """for (.*) only(:|,)""".r

  override def tryParseConditionVariant(factor: FactorInfo): Option[String] = {
    val textWithLineEndingsConvertedToSpaces = platformNeutralLineEndingRegex.replaceAllIn(factor.getText," ")
    val regexMatch = conditionVariantRegexMatch.findFirstMatchIn(textWithLineEndingsConvertedToSpaces)
    if(regexMatch.isEmpty) return None
    else {
      val variant = regexMatch.get.group(1)
      return Some(variant)
    }

  }
}

class OldStyleSubfactorParser extends NewStyleSubFactorParser
{

  override val paraStartRegex: Regex = """\([xiv]+\)""".r
  override def divideToSubfactors(factor: FactorInfo): List[SubFactorInfo] =
  {
    // small roman numerals
    // no further paras below small romans
    val lines = factor.getText.split(platformNeutralLineEndingRegex.regex).toList
    if (lines.size > 1) {
      val paras: List[List[String]] = FactorsParser.splitToParas(lines, paraStartRegex)
      val asClasses = FactorsParser.lineSetsToClass(paras.tail, paraStartRegex)
      val subFactorInfos = asClasses.map(c => SubFactorInfo(c.legalRef,stripLeadingPara(stripTrailingPunctuation(c.lines.mkString(Properties.lineSeparator)),paraStartRegex)))
      subFactorInfos
    }
    else {
      List()
    }
  }
}


