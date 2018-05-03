package au.gov.dva.sopapi.veaops

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import au.gov.dva.sopapi.veaops.interfaces.VeaOccurance
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

class VeaActivity(val shortName: String, val startDate: LocalDate, val endDate: Option[LocalDate], val specifiedAreas: List[SpecifiedArea], val qualifications: List[Qualification]) extends VeaOccurance {
  override def toJson(hostDet: VeaDetermination): JsonNode = {
    val om = new ObjectMapper()
    val root = om.createObjectNode()
    root.put("type",getDeterminationTypeString(hostDet))
    root.put("name",shortName)
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
    root.put("registerId", hostDet.registerId)
    root.put("sourceLink", s"${Constants.frlBaseUrl}/Details/${hostDet.registerId}")
    root.asInstanceOf[JsonNode]
  }
}
