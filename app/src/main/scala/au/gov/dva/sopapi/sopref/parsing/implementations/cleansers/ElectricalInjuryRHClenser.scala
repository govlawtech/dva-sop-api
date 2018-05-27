package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

object ElectricalInjuryRHClenser extends GenericTextClenser{

 // citation has missing space in the instrument number: '(No. 41of 2018).'

 override def clense(rawText: String): String =
 {
   super.clense(rawText)
    .replace("(No. 41of 2018)", "(No. 41 of 2018).")
 }

}
