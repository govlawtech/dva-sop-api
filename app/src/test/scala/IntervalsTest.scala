import java.time.{Duration, OffsetDateTime}

import au.gov.dva.sopapi.DateTimeUtils
import au.gov.dva.sopapi.sopref.datecalcs.{Interval, Intervals}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class IntervalsTest extends FunSuite {

  test("Get correct interval when bracket less than test period")
  {
    val result = Intervals.getSoPFactorTestIntervals(10,DateTimeUtils.localDateStringToActMidnightOdt("2000-01-01"),DateTimeUtils.localDateStringToActMidnightOdt("2001-01-01"))
    println(result)
    assert(result.head.start.isEqual(DateTimeUtils.localDateStringToActMidnightOdt("2000-01-01")) &&  result.head.end.isEqual(DateTimeUtils.localDateStringToActMidnightOdt("2001-01-01")))
  }

  test("Get correct intervals when bracket more than test period")
  {
    val result: List[Interval] = Intervals.getSoPFactorTestIntervals(10,DateTimeUtils.localDateStringToActMidnightOdt("2004-07-01"),DateTimeUtils.localDateStringToActMidnightOdt("2014-07-05"))
    val prettyPrinted = result.map(i => i.start.toLocalDate + "," + i.end.toLocalDate + "," + Duration.between(i.start,i.end).toDays)
    prettyPrinted.foreach(println(_))
  }

}
