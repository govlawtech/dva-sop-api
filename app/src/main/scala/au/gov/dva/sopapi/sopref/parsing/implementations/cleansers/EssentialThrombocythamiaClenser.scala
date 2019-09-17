package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

object EssentialThrombocythamiaClenser extends GenericTextClenser {

  // typo in source: missing open quotation in definitions section
  override def clense(rawText: String): String = {
    super.clense(rawText)
      .replace("death from essential thrombocythaemia\" in relation to a person includes","\"death from essential thrombocythaemia\" in relation to a person includes")
  }

}
