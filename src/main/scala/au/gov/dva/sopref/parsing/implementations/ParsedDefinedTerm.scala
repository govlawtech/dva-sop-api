package au.gov.dva.sopref.parsing.implementations

import au.gov.dva.sopref.interfaces.model.DefinedTerm

class ParsedDefinedTerm(term: String, definition: String) extends DefinedTerm {
  override def getTerm: String = term
  override def getDefinition: String = definition
}

