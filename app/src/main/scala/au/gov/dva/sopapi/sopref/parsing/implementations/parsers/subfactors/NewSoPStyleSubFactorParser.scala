package au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors

import au.gov.dva.sopapi.interfaces.model.Factor
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, SubFactorInfo}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.{FactorsParser, PreAugust2015Parser}
import au.gov.dva.sopapi.sopref.parsing.traits.{MiscRegexes, SubFactorParser}

import scala.util.Properties
import scala.util.matching.Regex

object NewSoPStyleSubFactorParser extends SubFactorParser with MiscRegexes {
  val regexForParaStart = """\([a-z]+\)""".r

  override def divideFactorsToSubFactors(factor: FactorInfo): List[SubFactorInfo] = {

    val regexMatch = conditionVariantRegexMatch.findFirstMatchIn(factor.getText)
    assert(regexMatch.isDefined)

    val lines = factor.getText.split(platformNeutralLineEndingRegex.regex).toList
    if (lines.size > 1) {
      val paras: List[List[String]] = FactorsParser.splitToParas(lines, regexForParaStart)
      val asClasses = FactorsParser.lineSetsToClass(paras.tail, regexForParaStart)
      val grouped: List[FactorsParser.MainPara] = FactorsParser.groupToMainParas(asClasses, List(), (_, _) => false)

      val subFactors = grouped.map(mp =>
        {
          val letter = mp.paraLinesParent.legalRef
          val lines = stripLeadingPara(stripTrailingPunctuation(mp.flattenLines),regexForParaStart)
          new SubFactorInfo(letter,lines)
        })
      subFactors
    }
    else {
      List()
    }
  }

  private def stripTrailingPunctuation(text : String) : String = {
    text.replaceFirst("((; or)|(;))$", "")
  }

  private def stripLeadingPara(text: String, regexForLeadingPara : Regex) : String = {
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

    val regexMatch = conditionVariantRegexMatch.findFirstMatchIn(factor.getText)
    if(regexMatch.isEmpty) return None
    else {
      val variant = regexMatch.get.group(1)
      return Some(variant)
    }

  }
}



