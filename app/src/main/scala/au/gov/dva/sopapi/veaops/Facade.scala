package au.gov.dva.sopapi.veaops

import java.io.ByteArrayInputStream
import java.time.{LocalDate, ZoneId}
import java.util.Optional

import au.gov.dva.sopapi.DateTimeUtils
import au.gov.dva.sopapi.veaops.interfaces.{VeaDeterminationOccurance, VeaOperationalServiceRepository}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.google.common.collect.ImmutableSet

import scala.collection.JavaConverters._
import scala.util.matching.Regex
import au.gov.dva.sopapi.veaops.Extensions._

// mainly for calling from Java
object Facade {


  def getResponseRangeQuery(startDate: LocalDate, endDate: LocalDate, veaRepo : VeaOperationalServiceRepository ): JsonNode = {
    val determinationQueryResults: Map[VeaDetermination, List[VeaDeterminationOccurance]] = VeaOperationalServiceQueries.getOpsAndActivitiesInRange(startDate, endDate, veaRepo.getDeterminations.asScala.toList)

    // warlike, non-warlike, hazardous, peacekeeping

    val flattenedDetrminationResults: List[(VeaDetermination, VeaDeterminationOccurance)] = determinationQueryResults.flatMap(t => t._2.map(o => (t._1,o))).toList

    val section6warlike: List[(VeaDetermination, VeaDeterminationOccurance)] = flattenedDetrminationResults.filter(i => i._1.isInstanceOf[WarlikeDetermination])
    val section6Nonwarlike: List[(VeaDetermination, VeaDeterminationOccurance)] = flattenedDetrminationResults.filter(i => i._1.isInstanceOf[NonWarlikeDetermination])
    val hazardous: List[(VeaDetermination, VeaDeterminationOccurance)] = flattenedDetrminationResults.filter(i => i._1.isInstanceOf[HazardousDetermination])

    val peacekeepingResults: List[VeaPeacekeepingActivity] = VeaOperationalServiceQueries.getPeacekeepingActivitiesInRange(
      startDate,
      endDate,
      veaRepo.getPeacekeepingActivities.asList().asScala.toList)


    val om = new ObjectMapper()
    val root = om.createObjectNode()

    val warlikeRoot = root.putArray("warlike")
    val warlikeObjects = section6warlike.sortBy(i => i._2.startDate.toEpochDay).map(i => i._2.toJson(i._1))
    warlikeRoot.addAll(warlikeObjects.asJavaCollection)

    val nonWarlikeRoot = root.putArray("non-warlike")
    val nonWarlikeObjects = section6Nonwarlike.sortBy(i => i._2.startDate.toEpochDay).map(i => i._2.toJson(i._1))
    nonWarlikeRoot.addAll(nonWarlikeObjects.asJavaCollection)

    val hazardousRoot = root.putArray("hazardous")
    val hazardousObjects = hazardous.sortBy(i => i._2.startDate.toEpochDay).map(i => i._2.toJson(i._1))
    hazardousRoot.addAll(hazardousObjects.asJavaCollection)

    val peacekeepingRoot = root.putArray("peacekeeping")
    val peacekeepingObjects = peacekeepingResults.map(p => p.toJson)
    peacekeepingRoot.addAll(peacekeepingObjects.asJavaCollection)

    root
  }


  def isOperational(identifierFromServiceHistory : String, startDate : LocalDate, endDate : Optional[LocalDate], veaRepo : VeaOperationalServiceRepository): Boolean = {
    veaRepo.isOperational(identifierFromServiceHistory,startDate,endDate.toScalaOption()).isOperational
  }

  def isWarlike(identifierFromServiceHistory : String, startDate : LocalDate, endDate : Optional[LocalDate], veaRepo : VeaOperationalServiceRepository): Boolean = {
    veaRepo.isWarlike(identifierFromServiceHistory,startDate,endDate.toScalaOption())
  }

  def deserialiseRepository(xmlBytes: Array[Byte]) : VeaOperationalServiceRepository = {
    val is = new ByteArrayInputStream(xmlBytes)
    val node = scala.xml.XML.load(is)
    val determinations = VeaDeserialisationUtils.DeterminationsfromXml(node)
    val peackeepingActivies = VeaDeserialisationUtils.PeacekeeepingActivitiesFromXml(node)
    is.close()
    new VeaOperationalServiceRepository {
      override def getPeacekeepingActivities: ImmutableSet[VeaPeacekeepingActivity] = ImmutableSet.copyOf(peackeepingActivies.asJava.iterator())

      override def getDeterminations: ImmutableSet[VeaDetermination] = ImmutableSet.copyOf(determinations.asJava.iterator())
    }



  }







}
