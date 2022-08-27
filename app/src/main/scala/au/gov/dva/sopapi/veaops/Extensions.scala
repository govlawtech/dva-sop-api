package au.gov.dva.sopapi.veaops

import java.time.{LocalDate, ZoneId}
import au.gov.dva.sopapi.DateTimeUtils
import au.gov.dva.sopapi.interfaces.model.Deployment
import au.gov.dva.sopapi.sopref.Operations
import au.gov.dva.sopapi.veaops.interfaces.{VeaDeterminationOccurance, VeaOperationalServiceRepository}

import scala.util.matching.Regex
import scala.collection.JavaConverters._

object Extensions {

  //  https://gist.github.com/julienroubieu/fbb7e1467ab44203a09f
  import java.util.Optional


  implicit class RichJavaOptional[T](javaOptional: Optional[T]) {
    def toScalaOption() = if (javaOptional.isPresent) Some(javaOptional.get()) else None
  }

  implicit class RichVeaOperationalServiceRepo(repo: VeaOperationalServiceRepository) {

    private def idFromServiceHistoryMatches(identifierFromServiceHistory: String, toMatch: HasMappings) = {


      if (toMatch.getPrimaryName.compareToIgnoreCase(identifierFromServiceHistory.trim) == 0) true
      else if (identifierFromServiceHistory.toLowerCase().contains(toMatch.getPrimaryName.toLowerCase())) true
      else {
        val mappings = toMatch.getMappings

        val atLeastOnMappingMatches =  mappings.exists(r => {
          val originalPattern = r.unanchored.pattern
          val anchoredRegex = new Regex(s"^$originalPattern$$")
          val isMatch = anchoredRegex.findFirstIn(identifierFromServiceHistory.trim).isDefined
          isMatch
        })

        atLeastOnMappingMatches
      }

    }

    case class VeaOperationalServiceTestResults(matchingDeterminations: List[(VeaDetermination, VeaDeterminationOccurance)], matchingPeacekeepingActivities: List[VeaPeacekeepingActivity]) {
      def isOperational = matchingDeterminations.nonEmpty || matchingPeacekeepingActivities.nonEmpty

    }


    def getOperationalTestResults(identifierFromServiceHistory: String, startDate: LocalDate, endDate: Option[LocalDate] = None): VeaOperationalServiceTestResults = {


      val determinationQueryResults: Map[VeaDetermination, List[VeaDeterminationOccurance]] = VeaOperationalServiceQueries.getOpsAndActivitiesInRange(startDate, endDate, repo.getDeterminations.asScala.toList)

      val flattenedDetrminationResults: List[(VeaDetermination, VeaDeterminationOccurance)] = determinationQueryResults.flatMap(t => t._2.map(o => (t._1, o))).toList

      val peacekeepingResults: List[VeaPeacekeepingActivity] = VeaOperationalServiceQueries.getPeacekeepingActivitiesInRange(
        startDate,
        endDate,
        repo.getPeacekeepingActivities.asList().asScala.toList)

      val matchingDeterminations: List[(VeaDetermination, VeaDeterminationOccurance)] = flattenedDetrminationResults
        .filter(r => r._1.isInstanceOf[WarlikeDetermination] || r._1.isInstanceOf[NonWarlikeDetermination] || r._1.isInstanceOf[HazardousDetermination])
        .filter(r => idFromServiceHistoryMatches(identifierFromServiceHistory, r._2))

      val matchingPeacekeeping = peacekeepingResults
        .filter(p => VeaOperationalServiceQueries.isWithinInterval(startDate, endDate, p))
        .filter(p => idFromServiceHistoryMatches(identifierFromServiceHistory, p))

      VeaOperationalServiceTestResults(matchingDeterminations, matchingPeacekeeping)
    }



    def isWarlike(identifierFromServiceHistory: String, startDate: LocalDate, endDate: Option[LocalDate]): Boolean = {
      val endDateOrNow = endDate.getOrElse(LocalDate.now(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE)))

      val determinationQueryResults: Map[VeaDetermination, List[VeaDeterminationOccurance]] = VeaOperationalServiceQueries.getOpsAndActivitiesInRange(startDate, endDate, repo.getDeterminations.asScala.toList)

      val flattenedDetrminationResults: List[(VeaDetermination, VeaDeterminationOccurance)] = determinationQueryResults.flatMap(t => t._2.map(o => (t._1, o))).toList

      val matchingDeterminations = flattenedDetrminationResults
        .filter(r => r._1.isInstanceOf[WarlikeDetermination])
        .filter(r => VeaOperationalServiceQueries.isWithinInterval(startDate, endDate, r._2))
        .filter(r => idFromServiceHistoryMatches(identifierFromServiceHistory, r._2))

      matchingDeterminations.nonEmpty
    }


  }

}
