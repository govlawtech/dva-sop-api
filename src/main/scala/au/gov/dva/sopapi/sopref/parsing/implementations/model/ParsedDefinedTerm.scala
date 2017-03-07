package au.gov.dva.sopapi.sopref.parsing.implementations.model

import au.gov.dva.sopapi.interfaces.model.DefinedTerm
import com.google.common.base.Objects

class ParsedDefinedTerm(term: String, definition: String) extends DefinedTerm {
  override def getTerm: String = term
  override def getDefinition: String = definition

  def canEqual(a: Any) = a.isInstanceOf[DefinedTerm]

  override def equals(definedTerm: Any): Boolean = {
    definedTerm match {
      case definedTerm: ParsedDefinedTerm =>
        definedTerm.canEqual(this) &&
          Objects.equal(this.getTerm, definedTerm.getTerm) &&
          Objects.equal(this.getDefinition, definedTerm.getDefinition)
      case _ => false
    }
  }

  override def hashCode(): Int = Objects.hashCode(this.getTerm, this.getDefinition)
}

