package au.gov.dva.sopapi.tests.parsers

import au.gov.dva.sopapi.sopref.data.Conversions
import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers.{GenericClenser, PostAug2015Clenser}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.util.Properties
;

@RunWith(classOf[JUnitRunner])
class CleanserTests extends FunSuite{

  test("Cleanse LS raw text") {
    val rawText = ParserTestUtils.resourceToString("lsConvertedToText.txt");
    val result = GenericClenser.clense(rawText)

    assert(result.length() > 0)
    System.out.println("START:")
    System.out.print(result)
    System.out.println("END")
  }

  test("Reinstate exponents")
  {
    val input = "4G = 4 x 9.81m/s\n2"
    val replaced = """m/s[\r\n]+2""".r.replaceAllIn(input,"m/s\u00B2")
    println(replaced)
  }

}
