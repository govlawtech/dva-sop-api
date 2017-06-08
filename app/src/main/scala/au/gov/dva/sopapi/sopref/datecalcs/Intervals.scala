package au.gov.dva.sopapi.sopref.datecalcs

import java.time.OffsetDateTime
import java.util

import au.gov.dva.sopapi.sopsupport.processingrules.Interval

import scala.collection.JavaConverters._



object Intervals {

  def getSopFactorTestIntervalsJavaList(numberOfYears: Int, bracketIntervalStart: OffsetDateTime, bracketIntervalEnd: OffsetDateTime): util.List[Interval] = {
    val r = getSoPFactorTestIntervals(numberOfYears,bracketIntervalStart,bracketIntervalEnd)
    r.asJava
  }

  def getSoPFactorTestIntervals(numberOfYears: Int, bracketIntervalStart: OffsetDateTime, bracketIntervalEnd: OffsetDateTime): List[Interval] = {
    if (bracketIntervalEnd.minusYears(numberOfYears).isBefore(bracketIntervalStart)) {
      return List( new Interval( bracketIntervalStart, bracketIntervalEnd))
    }
    slide(numberOfYears, bracketIntervalStart, bracketIntervalStart, bracketIntervalEnd)
  }



  private def slide(numberOfYears: Int, fixedStart: OffsetDateTime, slidingLower: OffsetDateTime, slidingUpper: OffsetDateTime): List[Interval] = {
    if (slidingUpper.minusYears(numberOfYears).isBefore(fixedStart)) {
      return List()
    }
    val newEndDate = slidingUpper.minusDays(1)
    val newStartDate = newEndDate.minusYears(numberOfYears)
    new Interval(newStartDate, slidingUpper) +: slide(numberOfYears, fixedStart, newStartDate, newEndDate)
  }
}
