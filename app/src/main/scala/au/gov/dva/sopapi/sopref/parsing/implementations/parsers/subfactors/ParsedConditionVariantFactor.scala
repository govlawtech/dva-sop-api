package au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors

import au.gov.dva.sopapi.interfaces.model.ConditionVariantFactor
import com.fasterxml.jackson.databind.JsonNode

class ParsedConditionVariantFactor(subPara: String, text: String) extends ConditionVariantFactor {
  override def getSubParagraph: String = subPara

  override def getText: String = text

}
