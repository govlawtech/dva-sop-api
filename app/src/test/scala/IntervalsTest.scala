import java.time.{Duration, OffsetDateTime}

import au.gov.dva.sopapi.DateTimeUtils
import au.gov.dva.sopapi.sopref.datecalcs.Intervals
import au.gov.dva.sopapi.sopsupport.processingrules.Interval
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class IntervalsTest extends FunSuite {

  test("Get correct interval when bracket less than test period")
  {
    val result = Intervals.getSoPFactorTestIntervals(10,DateTimeUtils.parseLocalDate("2000-01-01"),DateTimeUtils.parseLocalDate("2001-01-01"))
    println(result)
    assert(result.head.getStart.isEqual(DateTimeUtils.parseLocalDate("2000-01-01")) &&  result.head.getEnd.isEqual(DateTimeUtils.parseLocalDate("2001-01-01")))
  }

  test("Get correct intervals when bracket more than test period")
  {
    val result: List[Interval] = Intervals.getSoPFactorTestIntervals(10,DateTimeUtils.parseLocalDate("2004-07-01"),DateTimeUtils.parseLocalDate("2014-07-05"))
    val prettyPrinted = result.map(i => i.getStart + "," + i.getEnd + "," + Duration.between(i.getStart,i.getEnd).toDays)
    prettyPrinted.foreach(println(_))
  }

}
