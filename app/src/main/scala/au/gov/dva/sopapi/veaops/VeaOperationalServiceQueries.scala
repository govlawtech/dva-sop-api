package au.gov.dva.sopapi.veaops

import java.time.LocalDate

import au.gov.dva.sopapi.veaops.interfaces.{HasDates, VeaDeterminationOccurance, VeaOperationalServiceRepository}

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

  def getPeacekeepingActivitiesInRange(startDate : LocalDate, endDate: LocalDate, peacekeepingActivities: List[VeaPeacekeepingActivity]): List[VeaPeacekeepingActivity] = {
    peacekeepingActivities.filter(a => !a.initialDate.isAfter(endDate))
  }

  def getOpsAndActivitiesInRange(startOfTestRange: LocalDate, endOfTestRange: LocalDate, allDeterminations: List[VeaDetermination]): Map[VeaDetermination, List[VeaDeterminationOccurance]] = {
    allDeterminations
      .map(d => d -> getVeaOccurancesInInterval(startOfTestRange, endOfTestRange, d))
      .filter(i => i._2.nonEmpty)
      .toMap
  }

  private def getVeaOccurancesInInterval(startTestDate: LocalDate, endTestDate: LocalDate, veaDetermination: VeaDetermination) = {
    val occurances = veaDetermination.activities ++ veaDetermination.operations
    val inRange: List[VeaDeterminationOccurance] = occurances.filter(o => intervalsOverlap(startTestDate, endTestDate, o))
    inRange
  }

  def intervalsOverlap(startTestDate: LocalDate, endTestDate: LocalDate, potentiallyOpenEndedInterval: HasDates): Boolean = {
    if (potentiallyOpenEndedInterval.endDate.isEmpty) {
      !endTestDate.isBefore(potentiallyOpenEndedInterval.startDate)
    }
    else { // closed ended
      !startTestDate.isAfter(potentiallyOpenEndedInterval.endDate.get) && !endTestDate.isBefore(potentiallyOpenEndedInterval.startDate)
    }
  }
}
