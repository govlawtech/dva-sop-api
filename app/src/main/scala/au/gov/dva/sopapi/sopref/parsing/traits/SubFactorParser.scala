package au.gov.dva.sopapi.sopref.parsing.traits

import au.gov.dva.sopapi.interfaces.model.Factor
import au.gov.dva.sopapi.sopref.parsing.implementations.model.SubFactorInfo

trait SubFactorParser {
  def divideFactorsToSubFactors(factor: Factor) : List[SubFactorInfo]
  def tryParseConditionVariant(factor: Factor) : Option[String]
}
