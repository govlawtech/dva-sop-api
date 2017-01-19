package au.gov.dva.sopapi.sopref.parsing.implementations

import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, Factor, SubFactor}
import com.google.common.collect.{ImmutableList, ImmutableSet}

class ParsedFactor(paragraph : String, text: String, subFactors: List[SubFactor], definedTerms : Set[DefinedTerm]) extends Factor{
  override def getParagraph: String = paragraph

  override def getText: String = text

  override def getSubFactors: ImmutableList[SubFactor] = ImmutableList.copyOf(subFactors.toArray)

  override def getDefinedTerms: ImmutableSet[DefinedTerm] = ImmutableSet.copyOf(definedTerms.toArray)

  override def toString = s"ParsedFactor($getParagraph, $getText)"
}
