package au.gov.dva.sopapi.sopref.parsing.traits

import au.gov.dva.sopapi.interfaces.model.Factor
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, SubFactorInfo}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.FactorsParser

import scala.util.matching.Regex

trait SubFactorParser extends MiscRegexes {
  def divideFactorsToSubFactors(factor: FactorInfo) : List[SubFactorInfo]
  def tryParseConditionVariant(factor: FactorInfo) : Option[String]


}
