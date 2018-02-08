package au.gov.dva.sopapi.sopref.parsing.traits

import au.gov.dva.sopapi.interfaces.model.{ConditionVariant, Factor}
import au.gov.dva.sopapi.sopref.parsing.implementations.model.SubFactorInfo
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors.{OldSoPStyleSubFactorParser, ParsedConditionVariant, ParsedConditionVariantFactor}

trait ConditionVariantParser
{
  def parseConditionVariant(factor: Factor) : Option[ConditionVariant]
}

trait OldSoPStyleConditionVariantParser extends ConditionVariantParser
{
  override def parseConditionVariant(factor : Factor): Option[ConditionVariant] =  {
    val parser = new  OldSoPStyleSubFactorParser()
    val result = parser.tryParseConditionVariant(factor)
    if (result.isDefined)
      {
        val subFactorInfos: List[SubFactorInfo] = parser.divideFactorsToSubFactors(factor)
        val subFactors = subFactorInfos.map(si => new ParsedConditionVariantFactor(si.para,si.stripTrailingPunctuation.text))
        val conditionVariant = new ParsedConditionVariant(result.get,subFactors)
        return Some(conditionVariant)
      }
    else {
      return None
    }
  }
}

