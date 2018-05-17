package au.gov.dva.sopapi.veaops

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import au.gov.dva.sopapi.veaops.interfaces.{VeaDeterminationOccurance, toJson}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import scala.util.matching.Regex

class VeaOperation(val name: String, val startDate: LocalDate, val endDate: Option[LocalDate], val specifiedAreas: List[SpecifiedArea], val qualifications: List[Qualification], val mappings : Set[Regex]) extends VeaDeterminationOccurance with toJson {
  override def toString: String = name

  override def toJson(hostDet: VeaDetermination): JsonNode = {
    val om = new ObjectMapper()
    val root = om.createObjectNode()
    root.put("moniker",name)
    root.put("operationName", name)
    root.put("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
    if (endDate.isDefined) root.put("endDate", endDate.get.format(DateTimeFormatter.ISO_LOCAL_DATE))
    if (specifiedAreas.nonEmpty) {
      val specifiedAreasArray = root.putArray("specifiedAreas")
      specifiedAreas.foreach(sa => specifiedAreasArray.add(sa.desc))
    }
    if (qualifications.nonEmpty) {
      val qualificationsArray = root.putArray("qualifications")
      qualifications.foreach(q => qualificationsArray.add(q.text))
    }

    root.put("legalSource", s"${Constants.frlBaseUrl}/Details/${hostDet.registerId}")
    root.asInstanceOf[JsonNode]
  }

  override def getPrimaryName: String = name

  override def getMappings: Set[Regex] = mappings
}
