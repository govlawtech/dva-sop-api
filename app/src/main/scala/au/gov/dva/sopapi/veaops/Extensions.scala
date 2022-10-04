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

    case class VeaOperationalServiceTestResults(datesValidated: Boolean,matchingDeterminations: List[(VeaDetermination, VeaDeterminationOccurance)], matchingPeacekeepingActivities: List[VeaPeacekeepingActivity]) {
      def isOperational = matchingDeterminations.nonEmpty || matchingPeacekeepingActivities.nonEmpty
      def isWarlike = matchingDeterminations.exists(d => d._1.isInstanceOf[WarlikeDetermination])
    }

    def getOperationalTestResults(identifierFromServiceHistory: String, startDate: LocalDate, endDate: Option[LocalDate] = None, validateDates: Boolean): VeaOperationalServiceTestResults = {

      val determinationQueryResults: Map[VeaDetermination, List[VeaDeterminationOccurance]] = {
        validateDates match {
          case true => VeaOperationalServiceQueries.getVeaOpsAndActivitiesConsistentWithDates(startDate, endDate, repo.getDeterminations.asScala.toList)
          case false => repo.getDeterminations.asScala.map(d => (d,d.operations ++ d.activities)).toMap
        }
      }

      val flattenedDeterminationResults: List[(VeaDetermination, VeaDeterminationOccurance)] = determinationQueryResults.flatMap(t => t._2.map(o => (t._1, o))).toList

      val peacekeepingResults: List[VeaPeacekeepingActivity] = {
        validateDates match {
          case true => VeaOperationalServiceQueries.getPeacekeepingConsistentWithDates(
            startDate,
            endDate,
            repo.getPeacekeepingActivities.asList().asScala.toList)
          case false => repo.getPeacekeepingActivities.asScala.toList
        }
      }

      val matchingDeterminations: List[(VeaDetermination, VeaDeterminationOccurance)] = flattenedDeterminationResults
        .filter(r => r._1.isInstanceOf[WarlikeDetermination] || r._1.isInstanceOf[NonWarlikeDetermination] || r._1.isInstanceOf[HazardousDetermination])
        .filter(r => idFromServiceHistoryMatches(identifierFromServiceHistory, r._2))

      val matchingPeacekeeping = peacekeepingResults
        .filter(p => idFromServiceHistoryMatches(identifierFromServiceHistory, p))

      VeaOperationalServiceTestResults(validateDates, matchingDeterminations, matchingPeacekeeping)
    }


  }

}
