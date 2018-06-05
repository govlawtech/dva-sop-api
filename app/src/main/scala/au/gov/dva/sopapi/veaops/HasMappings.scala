package au.gov.dva.sopapi.veaops

import scala.util.matching.Regex

trait HasMappings {
  def getPrimaryName : String
  def getMappings : Set[Regex]
}
