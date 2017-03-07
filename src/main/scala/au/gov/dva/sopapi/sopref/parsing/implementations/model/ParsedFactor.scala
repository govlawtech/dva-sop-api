package au.gov.dva.sopapi.sopref.parsing.implementations.model

import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, Factor}
import com.google.common.base.Objects
import com.google.common.collect.ImmutableSet

class ParsedFactor(paragraph : String, text: String, definedTerms : Set[DefinedTerm]) extends Factor{
  override def getParagraph: String = paragraph

  override def getText: String = text

  override def getDefinedTerms: ImmutableSet[DefinedTerm] = ImmutableSet.copyOf(definedTerms.toArray)

  override def toString = s"ParsedFactor($getParagraph, $getText)"

  def canEqual(a: Any) = a.isInstanceOf[Factor]

  override def equals(factor: Any): Boolean = {
    factor match {
      case factor: ParsedFactor =>
        factor.canEqual(this) &&
          Objects.equal(this.getParagraph, factor.getParagraph) &&
          Objects.equal(this.getText, factor.getText) &&
          Objects.equal(this.getDefinedTerms, factor.getDefinedTerms)
      case _ => false
    }
  }

  override def hashCode(): Int = Objects.hashCode(this.paragraph, this.text, this.definedTerms)
}


