package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

object PrimaryMyelofibrosisClenser extends GenericTextClenser {

  // typo in source: missing open quotation in definitions section
  override def clense(rawText: String): String = {
    super.clense(rawText)
      .replace("death from primary myelofibrosis\" in relation to a person includes","\"death from primary myelofibrosis\" in relation to a person includes")
  }
}
