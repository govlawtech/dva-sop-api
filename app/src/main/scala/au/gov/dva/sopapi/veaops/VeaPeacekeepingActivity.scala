package au.gov.dva.sopapi.veaops

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import au.gov.dva.sopapi.veaops.interfaces.{HasDates, VeaDeterminationOccurance, toJson}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import scala.util.matching.Regex

case class  VeaPeacekeepingActivity(shortName: String, desc: String, initialDate: LocalDate, legalSource: String, regexMappings : Set[Regex]) extends HasDates {

  override def startDate: LocalDate = initialDate

  override def endDate: Option[LocalDate] = None

  def toJson = {
    val om = new ObjectMapper()
    val root = om.createObjectNode()
    root.put("moniker",shortName)
    root.put("startDate",initialDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
    root.put("description",desc)
    root.put("legalSource",legalSource)
    root
  }
}
