package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextCleanser


object HemorrhoidsCleanser extends GenericTextCleanser {

  // replace unconventional and unneccessary definition
  override def cleanse(rawText: String): String = {
    super.cleanse(rawText)
      .replace("haemorrhoids—see subsection 7(2).","")
  }
}





