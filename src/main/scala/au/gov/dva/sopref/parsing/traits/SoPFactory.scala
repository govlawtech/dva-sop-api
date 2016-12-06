package au.gov.dva.sopref.parsing.traits

import au.gov.dva.sopref.interfaces.model.SoP

trait SoPFactory {
  def create(registerId : String, clensedText : String) : SoP
}
