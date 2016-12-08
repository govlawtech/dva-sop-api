package au.gov.dva.sopref.parsing.implementations

import au.gov.dva.sopref.interfaces.model.{DefinedTerm, Factor}
import com.google.common.collect.ImmutableSet

class ParsedFactor(paragraph : String, text: String, definedTerms: Set[DefinedTerm]) extends Factor{
  override def getParagraph: String = paragraph

  override def getText: String = text

  override def getDefinedTerms: ImmutableSet[DefinedTerm] = ImmutableSet.copyOf(definedTerms.toArray)

  override def toString = s"ParsedFactor($getParagraph, $getText)"
}
