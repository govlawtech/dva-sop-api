package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser


object HemorrhoidsClenser extends GenericTextClenser {

  // replace unconventional and unneccessary definition
  override def clense(rawText: String): String = {
    super.clense(rawText)
      .replace("haemorrhoids—see subsection 7(2).","")
  }
}





