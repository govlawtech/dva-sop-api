package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser


object PagetsDiseaseClenser extends GenericTextClenser {

  // F2015L00255 does not specify standard; must be RH as F2015L00256 specifies BoP
  override def clense(rawText: String): String = {
    super.clense(rawText)
      .replace("The factor that must as a minimum exist in relation","The factor that must as a minimum exist in relation on a reasonable hypothesis")
  }
}

