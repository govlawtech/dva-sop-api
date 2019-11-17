package au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors

import au.gov.dva.sopapi.interfaces.model.{ConditionVariant, ConditionVariantFactor}
import com.google.common.collect.ImmutableList
import scala.collection.JavaConverters._

case class ParsedConditionVariant(name: String, variantFactors: List[ConditionVariantFactor]) extends ConditionVariant {
  override def getName: String = name
  override def getVariantFactors: ImmutableList[ConditionVariantFactor] = ImmutableList.copyOf(variantFactors.toArray)
}
