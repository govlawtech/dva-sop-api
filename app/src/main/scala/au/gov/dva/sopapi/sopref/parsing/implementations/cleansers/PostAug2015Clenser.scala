package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers.OsteoArthritisClenser.regexReplace
import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

import scala.util.Properties

object PostAug2015Clenser extends GenericTextClenser {
  override def clense(rawText: String): String = {

    List(rawText)
      .map(super.clense)
      .map(removeFootnotes)
      .map(removeHeadnote)
      .map(removePageGaps)
      .head
  }

  def removeHeadnote(raw: String): String = {
    regexReplace("""Schedule 1 - Dictionary""".r, raw)
  }

  def removeFootnotes(raw: String): String = {
    val regex = (""".*""" + Properties.lineSeparator
      +
      """.*""" + Properties.lineSeparator
      +
      """.*""" + Properties.lineSeparator
      +
      """([0-9]+ of )?[0-9]+""" + Properties.lineSeparator
      ).r
    regexReplace(regex, raw)
  }


}
