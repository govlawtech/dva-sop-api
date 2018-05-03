package au.gov.dva.sopapi.veaops

import java.time.LocalDate

import au.gov.dva.sopapi.veaops.interfaces.{HasDates, VeaOccurance}

object VeaOperationQueries {
  def getOpsAndActivitiesOnDate(testDate: LocalDate, allDeterminations: List[VeaDetermination]): Map[VeaDetermination, List[VeaOccurance]] = {
    def getThingsAtDate(testDate: LocalDate, things: List[VeaOccurance]): List[VeaOccurance] =
      things
        .filter(o => !o.startDate.isAfter(testDate) && (o.endDate.isEmpty || !o.endDate.get.isBefore(testDate)))

    val ongoingAtDate = allDeterminations
      .map(d => d -> (getThingsAtDate(testDate, d.operations) ++ getThingsAtDate(testDate, d.activities)))
      .filter(i => i._2.nonEmpty)
      .toMap

    ongoingAtDate
  }

  def getOpsAndActivitiesInRange(startOfTestRange: LocalDate, endOfTestRange: LocalDate, allDeterminations: List[VeaDetermination]): Map[VeaDetermination, List[VeaOccurance]] = {
    allDeterminations
      .map(d => d -> getVeaOccurancesInInterval(startOfTestRange, endOfTestRange, d))
      .filter(i => i._2.nonEmpty)
      .toMap
  }

  private def getVeaOccurancesInInterval(startTestDate: LocalDate, endTestDate: LocalDate, veaDetermination: VeaDetermination) = {
    val occurances = veaDetermination.activities ++ veaDetermination.operations
    val inRange: List[VeaOccurance] = occurances.filter(o => intervalsOverlap(startTestDate, endTestDate, o))
    inRange
  }

  private def intervalsOverlap(startTestDate: LocalDate, endTestDate: LocalDate, potentiallyOpenEndedInterval: HasDates): Boolean = {
    if (potentiallyOpenEndedInterval.endDate.isEmpty) {
      !endTestDate.isBefore(potentiallyOpenEndedInterval.startDate)
    }
    else { // closed ended
      !startTestDate.isAfter(potentiallyOpenEndedInterval.endDate.get) && !endTestDate.isBefore(potentiallyOpenEndedInterval.startDate)
    }
  }
}
