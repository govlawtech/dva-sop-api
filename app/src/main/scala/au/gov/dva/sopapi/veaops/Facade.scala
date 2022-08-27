package au.gov.dva.sopapi.veaops

import java.io.ByteArrayInputStream
import java.time.{LocalDate, ZoneId}
import java.util.Optional
import au.gov.dva.sopapi.DateTimeUtils
import au.gov.dva.sopapi.dtos.sopsupport.MilitaryOperationType
import au.gov.dva.sopapi.interfaces.CaseTrace
import au.gov.dva.sopapi.interfaces.model.{Deployment, JustifiedMilitaryActivity, MilitaryActivity}
import au.gov.dva.sopapi.servicedeterminations.VeaServiceDeterminations
import au.gov.dva.sopapi.veaops.interfaces.{VeaDeterminationOccurance, VeaOperationalServiceRepository}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.google.common.collect.ImmutableSet

import scala.collection.JavaConverters._
import scala.util.matching.Regex
import au.gov.dva.sopapi.veaops.Extensions._

import java.util

// mainly for calling from Java
object Facade {


  def getResponseRangeQuery(startDate: LocalDate, endDate: Optional[LocalDate], veaRepo : VeaOperationalServiceRepository ): JsonNode = {
    val determinationQueryResults: Map[VeaDetermination, List[VeaDeterminationOccurance]] = VeaOperationalServiceQueries.getOpsAndActivitiesInRange(startDate, endDate.toScalaOption(), veaRepo.getDeterminations.asScala.toList)

    // warlike, non-warlike, hazardous, peacekeeping

    val flattenedDetrminationResults: List[(VeaDetermination, VeaDeterminationOccurance)] = determinationQueryResults.flatMap(t => t._2.map(o => (t._1,o))).toList

    val section6warlike: List[(VeaDetermination, VeaDeterminationOccurance)] = flattenedDetrminationResults.filter(i => i._1.isInstanceOf[WarlikeDetermination])
    val section6Nonwarlike: List[(VeaDetermination, VeaDeterminationOccurance)] = flattenedDetrminationResults.filter(i => i._1.isInstanceOf[NonWarlikeDetermination])
    val hazardous: List[(VeaDetermination, VeaDeterminationOccurance)] = flattenedDetrminationResults.filter(i => i._1.isInstanceOf[HazardousDetermination])

    val peacekeepingResults: List[VeaPeacekeepingActivity] = VeaOperationalServiceQueries.getPeacekeepingActivitiesInRange(
      startDate,
      endDate.toScalaOption(),
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

  private def getMatchingActivitesForSingleDeployment(deployment: Deployment, veaRepo: VeaOperationalServiceRepository): List[MilitaryActivity]  = {
    // name matches and dates overlap
    var testResults = veaRepo.getOperationalTestResults(deployment.getOperationName(), deployment.getStartDate(), deployment.getEndDate().toScalaOption())
    def determinationToTypeToMilitaryOperationType (veaDetermination: VeaDetermination) = {
        veaDetermination match {
          case _ : WarlikeDetermination => MilitaryOperationType.Warlike
          case _ : NonWarlikeDetermination => MilitaryOperationType.NonWarlike
          case _ : HazardousDetermination => MilitaryOperationType.Hazardous
        }
    }

    def buildLegalSourceForDetermination (registerId : String) = {
      s"Federal Register of Legislation ID: $registerId"
    }

    val opsFromDeterminations = testResults.matchingDeterminations
      .map(det =>
        new MilitaryActivity(det._2.getPrimaryName,det._2.startDate,Optional.ofNullable(det._2.endDate.orNull),
          determinationToTypeToMilitaryOperationType(det._1),buildLegalSourceForDetermination(det._1.registerId)));

    val matchingPeacekeeping = testResults.matchingPeacekeepingActivities
      .map(pk =>
        new MilitaryActivity(pk.getPrimaryName,pk.startDate,Optional.ofNullable(pk.endDate.orNull),MilitaryOperationType.Peacekeeping,pk.legalSource)
      )

    (opsFromDeterminations ++ matchingPeacekeeping)

  }



  def getMatchingActivities(deployments: util.List[Deployment], veaRepo: VeaOperationalServiceRepository)  = {

    val deploymentToMilitaryActivityLists = deployments.asScala.map(d => (d,getMatchingActivitesForSingleDeployment(d,veaRepo)))
    val deploymentToMilitaryActivity = deploymentToMilitaryActivityLists.flatMap( t =>  t._2.map(a => (t._1,a)))
    val groupedByMilitaryActivity = deploymentToMilitaryActivity.groupBy(t => t._2)
    val justifiedMilitaryActivities = groupedByMilitaryActivity.map(t => new JustifiedMilitaryActivity(t._1,t._2.map(i => i._1).asJava) )
    justifiedMilitaryActivities.asJava
  }


  // just checks overlap with known operations - does not validate dates are consistent
  def isOperational(identifierFromServiceHistory : String, startDate : LocalDate, endDate : Optional[LocalDate], veaRepo : VeaOperationalServiceRepository): Boolean = {
    veaRepo.getOperationalTestResults(identifierFromServiceHistory,startDate,endDate.toScalaOption()).isOperational
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
