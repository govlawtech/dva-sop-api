package au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors

import au.gov.dva.sopapi.interfaces.model.Factor
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, SubFactorInfo}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.{FactorsParser, PreAugust2015Parser}
import au.gov.dva.sopapi.sopref.parsing.traits.{MiscRegexes, SubFactorParser}

import scala.util.Properties

class NewSoPStyleSubFactorParser extends SubFactorParser with MiscRegexes {
  val regexForParaStart = """\([a-z]+\)""".r

  override def divideFactorsToSubFactors(factor: Factor): List[SubFactorInfo] = {
    // divide to lines starting with part

    val lines = factor.getText.split(platformNeutralLineEndingRegex.regex).toList
    val paras: List[List[String]] = FactorsParser.splitToParas(lines,regexForParaStart)
    val asClasses = FactorsParser.lineSetsToClass(paras.tail,regexForParaStart)
    val grouped: List[FactorsParser.MainPara] = FactorsParser.groupToMainParas(asClasses,List(), (_, _) => false)
    val subFactors = grouped.map(mp => new SubFactorInfo(mp.paraLinesParent.legalRef,mp.flattenLines))
    subFactors
  }

  override def tryParseConditionVariant(factor: Factor): Option[String] = ???
}
