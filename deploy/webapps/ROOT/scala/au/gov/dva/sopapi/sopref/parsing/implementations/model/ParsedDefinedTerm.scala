package au.gov.dva.sopapi.sopref.parsing.implementations.model

import au.gov.dva.sopapi.interfaces.model.DefinedTerm

class ParsedDefinedTerm(term: String, definition: String) extends DefinedTerm {
  override def getTerm: String = term
  override def getDefinition: String = definition
}

