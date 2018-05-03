package au.gov.dva.sopapi.veaops

import java.io.{ByteArrayInputStream, InputStream}
import java.time.{LocalDate, ZoneId}
import java.time.format.DateTimeFormatter

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.google.common.collect.{ImmutableList, ImmutableSet}
import org.codehaus.jackson.map.JsonSerializer.None

import scala.collection.immutable
import scala.collection.JavaConverters._


object Constants
{
  val frlBaseUrl = "https://legislation.gov.au"
}

class VeaOperation(val name: String, val startDate: LocalDate, val endDate: Option[LocalDate], val specifiedAreas: List[SpecifiedArea], val qualifications: List[Qualification]) extends VeaOccurance {
  override def toString: String = name

  override def toJson(hostDet: VeaDetermination): JsonNode = {
    val om = new ObjectMapper()
    val root = om.createObjectNode()
    root.put("type",getDeterminationTypeString(hostDet))
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
    root.put("registerId", hostDet.registerId)
    root.put("sourceLink", s"${Constants.frlBaseUrl}/Details/${hostDet.registerId}")
    root.asInstanceOf[JsonNode]
  }
}

class SpecifiedArea(val desc: String)

class Qualification(val text: String)

class VeaActivity(val startDate: LocalDate, val endDate: Option[LocalDate], val specifiedAreas: List[SpecifiedArea], val qualifications: List[Qualification]) extends VeaOccurance {
  override def toJson(hostDet: VeaDetermination): JsonNode = {
    val om = new ObjectMapper()
    val root = om.createObjectNode()

    root.put("type",getDeterminationTypeString(hostDet))
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

class VeaActivityWithMoniker(todecorate: VeaActivity, moniker : String) extends VeaActivity(todecorate.startDate,todecorate.endDate,todecorate.specifiedAreas,todecorate.qualifications) {

  override def toJson(hostDetermination: VeaDetermination): JsonNode = {
    val baseNode = todecorate.toJson(hostDetermination).asInstanceOf[ObjectNode]
    baseNode.put("moniker", moniker)
    baseNode
  }
}

abstract class VeaDetermination(val registerId: String, val operations: List[VeaOperation], val activities: List[VeaActivity])

case class WarlikeDetermination(override val registerId: String, override val operations: List[VeaOperation], override val activities: List[VeaActivity]) extends VeaDetermination(registerId, operations, activities) {
  override def toString: String = s"$registerId: warlike, ${operations.length} ops, ${activities.length} activities"
}

case class NonWarlikeDetermination(override val registerId: String, override val operations: List[VeaOperation], override val activities: List[VeaActivity]) extends VeaDetermination(registerId, operations, activities) {

  override def toString: String = s"$registerId: non-warlike, ${operations.length} ops, ${activities.length} activities"
}

case class HazardousDetermination(override val registerId: String, override val operations: List[VeaOperation], override val activities: List[VeaActivity]) extends VeaDetermination(registerId, operations, activities) {
  override def toString: String = s"$registerId: hazardous, ${operations.length} ops, ${activities.length} activities"
}

trait HasDates {
  def startDate: LocalDate
  def endDate: Option[LocalDate]
}


trait VeaOccurance extends HasDates with ToJson

trait ToJson {
  def toJson(hostDetermination: VeaDetermination): JsonNode

  def getDeterminationTypeString(det : VeaDetermination) = {
    det match {
      case _: WarlikeDetermination => "warlike"
      case _: NonWarlikeDetermination => "non-warlike"
      case _: HazardousDetermination => "hazardous"
    }


  }






}