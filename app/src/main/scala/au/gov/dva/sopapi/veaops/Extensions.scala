package au.gov.dva.sopapi.veaops

import java.time.{LocalDate, ZoneId}

import au.gov.dva.sopapi.DateTimeUtils
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

    private def idFromServiceHistoryMatches(identifierFromServiceHistory : String, toMatch : HasMappings) = {

      toMatch.getPrimaryName.compareToIgnoreCase(identifierFromServiceHistory.trim) == 0 ||
        toMatch.getMappings.exists(r => {
          val originalPattern = r.unanchored.pattern
          val anchoredRegex = new Regex("^" +  "originalPattern" + "$")
          val isMatch = anchoredRegex.findFirstIn(identifierFromServiceHistory.trim).isDefined
          isMatch
        })
    }

    def isOperational(identifierFromServiceHistory : String, startDate : LocalDate, endDate : Option[LocalDate]): Boolean = {


      val endDateOrNow = endDate.getOrElse(LocalDate.now(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE)))

      val determinationQueryResults: Map[VeaDetermination, List[VeaDeterminationOccurance]] = VeaOperationalServiceQueries.getOpsAndActivitiesInRange(startDate, endDateOrNow, repo.getDeterminations.asScala.toList)

      val flattenedDetrminationResults: List[(VeaDetermination, VeaDeterminationOccurance)] = determinationQueryResults.flatMap(t => t._2.map(o => (t._1,o))).toList

      val peacekeepingResults: List[VeaPeacekeepingActivity] = VeaOperationalServiceQueries.getPeacekeepingActivitiesInRange(
        startDate,
        endDateOrNow,
        repo.getPeacekeepingActivities.asList().asScala.toList)

      val matchingDeterminations = flattenedDetrminationResults
        .filter(r => r._1.isInstanceOf[WarlikeDetermination] || r._1.isInstanceOf[NonWarlikeDetermination] || r._1.isInstanceOf[HazardousDetermination])
        .filter(r => VeaOperationalServiceQueries.intervalsOverlap(startDate,endDateOrNow,r._2))
        .filter(r => idFromServiceHistoryMatches(identifierFromServiceHistory,r._2))

      val matchingPeacekeeping = peacekeepingResults
        .filter(p => VeaOperationalServiceQueries.intervalsOverlap(startDate,endDateOrNow,p))
        .filter(p => idFromServiceHistoryMatches(identifierFromServiceHistory,p))

      matchingDeterminations.nonEmpty || matchingPeacekeeping.nonEmpty

    }


    def isWarlike(identifierFromServiceHistory : String, startDate: LocalDate, endDate : Option[LocalDate]): Boolean =
    {
      val endDateOrNow = endDate.getOrElse(LocalDate.now(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE)))

      val determinationQueryResults: Map[VeaDetermination, List[VeaDeterminationOccurance]] = VeaOperationalServiceQueries.getOpsAndActivitiesInRange(startDate, endDateOrNow, repo.getDeterminations.asScala.toList)

      val flattenedDetrminationResults: List[(VeaDetermination, VeaDeterminationOccurance)] = determinationQueryResults.flatMap(t => t._2.map(o => (t._1,o))).toList

      val matchingDeterminations = flattenedDetrminationResults
        .filter(r => r._1.isInstanceOf[WarlikeDetermination])
        .filter(r => VeaOperationalServiceQueries.intervalsOverlap(startDate,endDateOrNow,r._2))
        .filter(r => idFromServiceHistoryMatches(identifierFromServiceHistory,r._2))

      matchingDeterminations.nonEmpty
    }


  }

}
