package au.gov.dva.sopref.parsing.traits

import au.gov.dva.sopref.interfaces.model.SoP

trait SoPParser {
  def parse(rawText : String) : SoP
}
















