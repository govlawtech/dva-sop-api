package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextCleanser

import scala.util.Properties

object OsteoArthritisCleanser extends GenericTextCleanser{
  override def cleanse(rawText: String): String = {

    // fix typo in legislation
    super.cleanse(rawText)
      .replace("(i) using a hand-held, vibrating, percussive,","(ii) using a hand-held, vibrating, percussive,")

  }




}
