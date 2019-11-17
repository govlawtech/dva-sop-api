package au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors

import java.util.Optional

import au.gov.dva.sopapi.interfaces.model.{ConditionVariant, DefinedTerm, Factor}
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{ConditionVariantInfo, FactorInfo, SubFactorInfo}
import au.gov.dva.sopapi.sopref.parsing.traits.SubFactorParser
import com.google.common.collect.ImmutableSet

object ConditionVariants {

  def addornFactor(f : FactorInfo, isOldStyle : Boolean) : FactorInfo = {

    val variantParser = isOldStyle match {
      case true => new OldStyleSubfactorParser()
      case false => new NewStyleSubFactorParser()
    }

    val variant: Option[String] = variantParser.tryParseConditionVariant(f)
    if (variant.isDefined)
      {
        val subFactors = variantParser.divideToSubfactors(f)
          .map(sfi => new ParsedConditionVariantFactor(sfi.para,sfi.text))

        val conditionVariant = new ParsedConditionVariant(variant.get,subFactors)

        return new FactorInfo {override def getText: String = f.getText

          override def getLetter: String = f.getLetter

          override def getConditionVariant: Option[ConditionVariant] = Some(conditionVariant)
        }
      }
    else {
      return f
    }
  }
}
