package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers


import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser


object DropTocClenser extends  GenericTextClenser {

  val tocRegex = """Contents"""

  //override def clense(rawText: String): String = {

  //}
}
