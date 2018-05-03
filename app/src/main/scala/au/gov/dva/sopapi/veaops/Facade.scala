package au.gov.dva.sopapi.veaops

import java.io.ByteArrayInputStream
import java.time.LocalDate

import au.gov.dva.sopapi.veaops.interfaces.VeaOccurance
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.google.common.collect.ImmutableSet

import scala.collection.JavaConverters._

object Facade {

  def getResponseRangeQuery(startDate: LocalDate, endDate: LocalDate, allDeterminations: ImmutableSet[VeaDetermination]): JsonNode = {
    val asScala = allDeterminations.asScala.toList
    getResponseRangeQuery(startDate,endDate,asScala)
  }

  def getResponseRangeQuery(startDate: LocalDate, endDate: LocalDate, allDeterminations: List[VeaDetermination]): JsonNode = {
    val queryResults: Map[VeaDetermination, List[VeaOccurance]] = VeaOperationQueries.getOpsAndActivitiesInRange(startDate, endDate, allDeterminations)
    val flattened: List[(VeaDetermination, VeaOccurance)] = queryResults.flatMap(t => t._2.map(o => (t._1,o))).toList
    val orderedByStartDate = flattened.sortBy(i => i._2.startDate.toEpochDay)
    val jsonNodes: List[JsonNode] = orderedByStartDate.map(t => t._2.toJson(t._1))
    val om = new ObjectMapper()
    val rootArray = om.createArrayNode()
    rootArray.addAll(jsonNodes.asJavaCollection)
    rootArray
  }

  def deserialiseDeterminations(xmlBytes: Array[Byte]) : ImmutableSet[VeaDetermination] =
  {
    val is = new ByteArrayInputStream(xmlBytes)
    val node = scala.xml.XML.load(is)
    val determinations = VeaDeserialisationUtils.DeterminationsfromXml(node)
    is.close()
    ImmutableSet.copyOf(determinations.asJava.iterator())
  }



}
