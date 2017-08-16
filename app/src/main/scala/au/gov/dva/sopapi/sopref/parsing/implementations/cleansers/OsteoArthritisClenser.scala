package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

import scala.util.Properties

object OsteoArthritisClenser extends GenericTextClenser{
  override def clense(rawText: String): String = {

    // fix typo in legislation
    super.clense(rawText)
      .replace("(i) using a hand-held, vibrating, percussive,","(ii) using a hand-held, vibrating, percussive,")

  }


}

