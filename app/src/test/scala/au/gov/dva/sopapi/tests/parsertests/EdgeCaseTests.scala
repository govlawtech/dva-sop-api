package au.gov.dva.sopapi.tests.parsertests

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.interfaces.model.Factor
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.traits.PreAug2015FactorsParser
import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EdgeCaseTests extends FunSuite {


  test("Sleep apnoea factors do not contain para number at beginning of text")
  {
     val parsed = ParserTestUtils.executeWholeParsingPipeline("F2013L01133", "allSops/F2013L01133.pdf")
     val factors = parsed.getOnsetFactors.toArray()
     assert(factors.forall(f => !f.asInstanceOf[Factor].getText.startsWith("(")))

  }

  test("anxiety disorder compilation parses")
  {

    val parsed = ParserTestUtils.executeWholeParsingPipeline("F2016C00973", "allSops/F2016C00973.pdf")
  }

  test("Fracture")
  {
    val parsed = ParserTestUtils.executeWholeParsingPipeline("F2015L01343", "allSops/F2015L01343.pdf")
  }

  test("Pagets disease RH sop")
  {
    val parsed = ParserTestUtils.executeWholeParsingPipeline("F2015L00255","allSops/F2015L00255.pdf")
    println(parsed)
  }


}
