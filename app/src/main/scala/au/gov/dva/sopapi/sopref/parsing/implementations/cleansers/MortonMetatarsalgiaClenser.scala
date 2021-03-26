package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

object MortonMetatarsalgiaClenser extends GenericTextClenser {

 val toReplace =  "factors set out in subsections 9(6) to (11) apply only"
 val replacement = "factors set out in subsections 9(6) to 9(11) apply only"

 override def clense(rawText: String): String = super.clense(rawText).replace(toReplace,replacement)
}


