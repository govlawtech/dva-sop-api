package au.gov.dva.sopapi.tests.parsertests

import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CorrectlyIdentfyAggravationFactors extends FunSuite {

  test("Correctly extract aggravation paras in post 2015 sops")
  {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2017L00889","allSops/F2017L00889.pdf")
    val aggravationFactors = result.getAggravationFactors()
    assert(aggravationFactors.size() == 6)
    println(result)

  }

}
