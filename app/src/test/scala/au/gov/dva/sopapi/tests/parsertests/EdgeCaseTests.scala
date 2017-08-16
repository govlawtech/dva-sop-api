package au.gov.dva.sopapi.tests.parsertests

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.interfaces.model.Factor
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
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
    println(TestUtils.prettyPrint(StoredSop.toJson(parsed)))
  }


  test("Presbyopia")
  {
    // F2017L00172 and F2017L00173
    val rhparsed = ParserTestUtils.executeWholeParsingPipeline("F2017L00172","allSops/F2017L00172.pdf")
    println(TestUtils.prettyPrint(StoredSop.toJson(rhparsed)))

    val bopparsed = ParserTestUtils.executeWholeParsingPipeline("F2017L00173","allSops/F2017L00173.pdf")
    println(TestUtils.prettyPrint(StoredSop.toJson(bopparsed)))


  }

  test("EssentialThrombocythamia")
  {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2013L00411","allSops/F2013L00411.pdf")

  }

  test("Primary myelofibrosis")
  {

    val result = ParserTestUtils.executeWholeParsingPipeline("F2013L00412","allSops/F2013L00412.pdf")

  }

  test("blephartis")
  {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2010L02303","allSops/F2010L02303.pdf")
  }

  test("Single factor sop")
  {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2013L01127","allSops/F2013L01127.pdf")
  }

  test("Parse citation with - and numbers")
  {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2014L01837","allSops/F2014L01837.pdf")
    assert(result.getConditionName.contentEquals("alpha-1 antitrypsin deficiency"))
  }

}
