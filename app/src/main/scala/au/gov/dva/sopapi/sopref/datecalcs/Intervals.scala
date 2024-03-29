package au.gov.dva.sopapi.sopref.datecalcs

import java.time.LocalDate
import java.util

import au.gov.dva.sopapi.sopsupport.processingrules.Interval

import scala.annotation.tailrec
import scala.collection.JavaConverters._



object Intervals {

  def getSopFactorTestIntervalsJavaList(numberOfYears: Int, bracketIntervalStart: LocalDate, bracketIntervalEnd: LocalDate): util.List[Interval] = {
    val r = getSoPFactorTestIntervals(numberOfYears,bracketIntervalStart,bracketIntervalEnd)
    r.asJava
  }



  def getSoPFactorTestIntervals(numberOfYears: Int, bracketIntervalStart: LocalDate, bracketIntervalEnd: LocalDate): List[Interval] = {
    if (bracketIntervalEnd.minusYears(numberOfYears).isBefore(bracketIntervalStart)) {
      return List( new Interval( bracketIntervalStart, bracketIntervalEnd))
    }

    slide(numberOfYears, bracketIntervalStart, bracketIntervalStart, bracketIntervalEnd, List())
  }

  @tailrec private def slide(numberOfYears: Int, fixedStart: LocalDate, slidingLower: LocalDate, slidingUpper: LocalDate, acc: List[Interval] ): List[Interval] = {
    if (slidingUpper.minusYears(numberOfYears).isBefore(fixedStart)) {
      return acc
    }
    val newEndDate = slidingUpper.minusDays(1)
    val newStartDate = newEndDate.minusYears(numberOfYears)

    slide(numberOfYears, fixedStart, newStartDate, newEndDate, new Interval(newStartDate,newEndDate) +: acc)
  }
}
