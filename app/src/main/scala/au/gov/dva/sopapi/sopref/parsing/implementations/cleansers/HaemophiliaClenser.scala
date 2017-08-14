package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

object HaemophiliaClenser extends GenericTextClenser {
  override def clense(rawText: String): String = {
    super.clense(rawText)
      .replace("death from haemophilia\" in relation to a person includes","\"death from haemophilia\" in relation to a person includes")
  }
}
