package au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors

import au.gov.dva.sopapi.interfaces.model.Factor
import au.gov.dva.sopapi.sopref.parsing.implementations.model.SubFactorInfo
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.FactorsParser
import au.gov.dva.sopapi.sopref.parsing.traits.{MiscRegexes, SubFactorParser}


class OldSoPStyleSubFactorParser extends SubFactorParser with MiscRegexes {

  val regexForParaStart = """\([a-z]+\)""".r

  override def divideFactorsToSubFactors(factor: Factor): List[SubFactorInfo] = {


    val lines = factor.getText.split(platformNeutralLineEndingRegex.regex).toList
    val lineSets = FactorsParser.splitToParas(lines,regexForParaStart)
    val lineSetsWithoutSectionHead = lineSets.drop(1)
    val asClasses = FactorsParser.lineSetsToClass(lineSetsWithoutSectionHead,regexForParaStart)
    val groupedAsMainParas = FactorsParser.groupToMainParas(asClasses,List(),(_,_) => false)
    val subFactors = groupedAsMainParas.map(mp => new SubFactorInfo(mp.paraLinesParent.legalRef, mp.flattenLines).stripTrailingPunctuation)
    subFactors
  }

  override def tryParseConditionVariant(factor: Factor): Option[String] = ???
}
