package au.gov.dva.sopapi.veaops.interfaces

import au.gov.dva.sopapi.veaops.{HazardousDetermination, NonWarlikeDetermination, VeaDetermination, WarlikeDetermination}
import com.fasterxml.jackson.databind.JsonNode

trait toJson {
  def toJson(veaDetermination: VeaDetermination): JsonNode

  def getDeterminationTypeString(det : VeaDetermination) = {
    det match {
      case _: WarlikeDetermination => "warlike"
      case _: NonWarlikeDetermination => "non-warlike"
      case _: HazardousDetermination => "hazardous"
    }


  }
}
