package au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors

import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.interfaces.model.Factor
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, SubFactorInfo}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.FactorsParser
import au.gov.dva.sopapi.sopref.parsing.traits.{MiscRegexes, SubFactorParser}


object OldSoPStyleSubFactorParser extends SubFactorParser with MiscRegexes {

  val regexForParaStart = """\([a-z]+\)""".r

  override def divideFactorsToSubFactors(factor: FactorInfo): List[SubFactorInfo] = {
      NewSoPStyleSubFactorParser.divideFactorsToSubFactors(factor)
  }

  val conditionVariantRegexMatch = """for (.*) only(:|,)""".r
  override def tryParseConditionVariant(factor: FactorInfo): Option[String] = {

    val firstLine = factor.getText.split(platformNeutralLineEndingRegex.regex).headOption
    assert (!firstLine.isEmpty, "Cannot find first line in factor text: " + factor.getText)

    val regexMatch = conditionVariantRegexMatch.findFirstMatchIn(firstLine.get)
    if(regexMatch.isEmpty) return None
    else {
      val variant = regexMatch.get.group(1)
      return Some(variant)
    }
  }
}
