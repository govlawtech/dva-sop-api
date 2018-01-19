package au.gov.dva.sopapi.sopref.parsing.traits

import au.gov.dva.sopapi.interfaces.model.Factor
import au.gov.dva.sopapi.sopref.parsing.implementations.model.SubFactorInfo
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.FactorsParser

import scala.util.matching.Regex

trait SubFactorParser extends MiscRegexes {
  def divideFactorsToSubFactors(factor: Factor) : List[SubFactorInfo]
  def tryParseConditionVariant(factor: Factor) : Option[String]



}
