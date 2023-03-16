package au.gov.dva.sopapi.veaops

import au.gov.dva.sopapi.DateTimeUtils
import au.gov.dva.sopapi.interfaces.model.MaybeOpenEndedInterval
import au.gov.dva.sopapi.sopsupport.processingrules.HasDateRangeImpl

import java.time.LocalDate
import au.gov.dva.sopapi.veaops.interfaces.{HasDates, VeaDeterminationOccurance, VeaOperationalServiceRepository}

import java.util.Optional

class OpenEndedInterval(startDate: LocalDate, endDate: Option[LocalDate]) extends MaybeOpenEndedInterval {
  override def getStartDate: LocalDate = startDate
  override def getEndDate: Optional[LocalDate] = Optional.ofNullable(endDate.orNull)
}

object VeaOperationalServiceQueries {

  def getOpsAndActivitiesOnDate(testDate: LocalDate, allDeterminations: List[VeaDetermination]): Map[VeaDetermination, List[VeaDeterminationOccurance]] = {
    def getThingsAtDate(testDate: LocalDate, things: List[VeaDeterminationOccurance]): List[VeaDeterminationOccurance] =
      things
        .filter(o => !o.startDate.isAfter(testDate) && (o.endDate.isEmpty || !o.endDate.get.isBefore(testDate)))

    val ongoingAtDate = allDeterminations
      .map(d => d -> (getThingsAtDate(testDate, d.operations) ++ getThingsAtDate(testDate, d.activities)))
      .filter(i => i._2.nonEmpty)
      .toMap

    ongoingAtDate
  }

  def getPeacekeepingConsistentWithDates(startDate : LocalDate, endDate: Option[LocalDate], peacekeepingActivities: List[VeaPeacekeepingActivity]): List[VeaPeacekeepingActivity] = {
    peacekeepingActivities.filter(a => DateTimeUtils.IsFirstOpenEndedIntervalWithinSecond(new OpenEndedInterval(startDate,endDate),new OpenEndedInterval(a.startDate,a.endDate)))
  }



  def getVeaOpsAndActivitiesConsistentWithDates(startOfTestRange: LocalDate, endOfTestRange: Option[LocalDate], allDeterminations: List[VeaDetermination]): Map[VeaDetermination, List[VeaDeterminationOccurance]] = {
    allDeterminations
      .map(d => d -> getVeaOccurancesInInterval(startOfTestRange, endOfTestRange, d))
      .filter(i => i._2.nonEmpty)
      .toMap
  }

  private def getVeaOccurancesInInterval(startTestDate: LocalDate, endTestDate: Option[LocalDate], veaDetermination: VeaDetermination) = {
    val occurances = veaDetermination.activities ++ veaDetermination.operations
    val inRange: List[VeaDeterminationOccurance] = occurances.filter(o =>  isWithinInterval(startTestDate, endTestDate, o))
    inRange
  }


  def isWithinInterval(startTestDate: LocalDate, endTestDate: Option[LocalDate], potentiallyOpenEndedInterval: HasDates): Boolean = {
    return DateTimeUtils.IsFirstOpenEndedIntervalWithinSecond(new OpenEndedInterval(startTestDate,endTestDate),new OpenEndedInterval(potentiallyOpenEndedInterval.startDate,potentiallyOpenEndedInterval.endDate))
  }
}
