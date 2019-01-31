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


  test("Sleep apnoea factors do not contain para number at beginning of text") {
    val parsed = ParserTestUtils.executeWholeParsingPipeline("F2013L01133", "allSops/F2013L01133.pdf")
    val factors = parsed.getOnsetFactors.toArray()
    assert(factors.forall(f => !f.asInstanceOf[Factor].getText.startsWith("(")))

  }

  test("anxiety disorder compilation parses") {

    val parsed = ParserTestUtils.executeWholeParsingPipeline("F2016C00973", "allSops/F2016C00973.pdf")
  }

  test("Fracture") {
    val parsed = ParserTestUtils.executeWholeParsingPipeline("F2015L01343", "allSops/F2015L01343.pdf")
    println(TestUtils.prettyPrint(StoredSop.toJson(parsed)))

  }

  test("Pagets disease RH sop") {
    val parsed = ParserTestUtils.executeWholeParsingPipeline("F2015L00255", "allSops/F2015L00255.pdf")
    println(TestUtils.prettyPrint(StoredSop.toJson(parsed)))
  }


  test("Presbyopia") {
    // F2017L00172 and F2017L00173
    val rhparsed = ParserTestUtils.executeWholeParsingPipeline("F2017L00172", "allSops/F2017L00172.pdf")
    println(TestUtils.prettyPrint(StoredSop.toJson(rhparsed)))

    val bopparsed = ParserTestUtils.executeWholeParsingPipeline("F2017L00173", "allSops/F2017L00173.pdf")
    println(TestUtils.prettyPrint(StoredSop.toJson(bopparsed)))


  }

  test("EssentialThrombocythamia") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2013L00411", "allSops/F2013L00411.pdf")

  }

  test("Primary myelofibrosis") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2013L00412", "allSops/F2013L00412.pdf")
  }

  test("blephartis") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2010L02303", "allSops/F2010L02303.pdf")
  }


  test("Single factor sop") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2013L01127", "allSops/F2013L01127.pdf")
  }

  test("Parse citation with - and numbers") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2014L01837", "allSops/F2014L01837.pdf")
    assert(result.getConditionName.contentEquals("alpha-1 antitrypsin deficiency"))
  }


  test("Sub para in disease definition section does not throw section splitter") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2009C00524", "allSops/F2009C00524.pdf")
  }


  ignore("Correct section splitting in new style sop") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2016L00261", "allSops/F2016L00261.pdf")
  }

  test("Title extracted correctly in compilations") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2015C00914", "allSops/F2015C00914.pdf")
  }

  test("benign prostatic hyperplasia factors correctly categorised") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2016L00240", "allSops/F2016L00240.pdf")
    assert(result.getAggravationFactors().size() == 2)
  }

  test("sarcoidosis factors for bop correctly categorised") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2016L01143", "allSops/F2016L01143.pdf")
    assert(result.getAggravationFactors().size() == 2)
  }

  test("S's disease factors for bop correctly categorised") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2016L01344", "allSops/F2016L01344.pdf")
    assert(result.getAggravationFactors().size() == 2)
  }


  test("HEPATITIS B correctly categorised") {
    val Rhresult = ParserTestUtils.executeWholeParsingPipeline("F2017L00001", "allSops/F2017L00001.pdf")
    assert(Rhresult.getAggravationFactors().size() == 3 && Rhresult.getOnsetFactors().size() == 3) // overlapping factor

    val BoPresult = ParserTestUtils.executeWholeParsingPipeline("F2017L00007", "allSops/F2017L00007.pdf")
    assert(BoPresult.getAggravationFactors().size() == 3 && BoPresult.getOnsetFactors().size() == 2) // overlapping factor
  }

  test("sudden unexplained death") {
    val r = ParserTestUtils.executeWholeParsingPipeline("F2013L01646", "allSops/F2013L01646.pdf")
  }

  test("Where are my 'other definitions'") {
    var r = ParserTestUtils.executeWholeParsingPipeline("F2016C00252", "allSops/F2016C00252.pdf")
  }

  test("No factor truncation in 2016 compilation with short footnote") {
    var r = ParserTestUtils.executeWholeParsingPipeline("F2016C00270", "allSops/F2016C00270.pdf")
    assert(r.getOnsetFactors.get(1).getText.endsWith("lymphocytic lymphoma"))
  }


  test("Definitions section with two numbers") {
    var r = ParserTestUtils.executeWholeParsingPipeline("F2016L00265", "allSops/F2016L00265.pdf")

  }

  test("Omitted definitions number in F2017C00198 is handled") {
    var r = ParserTestUtils.executeWholeParsingPipeline("F2017C00198", "allSops/F2017C00198.pdf")
    println(TestUtils.prettyPrint(StoredSop.toJson(r)))
  }

  test("Third level factors parsed correctly") {
    var r = ParserTestUtils.executeWholeParsingPipeline("F2010L02304", "allSops/F2010L02304.pdf")
    println(TestUtils.prettyPrint(StoredSop.toJson(r)))

  }

  test("Chronic obstructive pulmonary with many subparas parsed correctly") {
    var r = ParserTestUtils.executeWholeParsingPipeline("F2015C00915", "allSops/F2015C00915.pdf")
    println(TestUtils.prettyPrint(StoredSop.toJson(r)))

  }

  test("Non unicode superscripts for iodine handled correctly") {
    val r = ParserTestUtils.executeWholeParsingPipeline("F2013L00728", "allSops/F2013L00728.pdf")

  }

  test("Non unicode superscripts for yttrium handled correctly") {
    val r = ParserTestUtils.executeWholeParsingPipeline("F2013L00720", "allSops/F2013L00720.pdf")
  }


  test("Tall footnotes in new compliations cropped") {
    var r = ParserTestUtils.executeWholeParsingPipeline("F2017C00077", "allSops/F2017C00077.pdf")
    println(TestUtils.prettyPrint(StoredSop.toJson(r)))
  }

  test("Typo in definitions in multi illness fixed") {
    val r = ParserTestUtils.executeWholeParsingPipeline("F2014L00525", "allSops/F2014L00525.pdf")

  }

  test("Typo in sinus barotrauma addressed") {
    val r = ParserTestUtils.executeWholeParsingPipeline("F2017C00075", "allSops/F2017C00075.pdf")
  }

  test("malig neoplastm of the overy") {
    val r = ParserTestUtils.executeWholeParsingPipeline("F2018L00010", "allSops/F2018L00010.pdf")
  }

  test("extract icd codes from MALIGNANT NEOPLASM OF THE CEREBRAL MENINGES")
  {
    val r = ParserTestUtils.executeWholeParsingPipeline("F2018L00001", "allSops/F2018L00001.pdf")
    val icdCodes = r.getICDCodes
    assert(icdCodes.size() == 2)
  }

  test("attempted suicide 2018 compilation correctly parsed")
  {
    // missing the 1 in front of definitions section
    val r = ParserTestUtils.executeWholeParsingPipeline("F2018C00189","allSops/F2018C00189.pdf")
  }


  test("cat 1B updates fail 1 - gastric ulcer")
  {
      val r = ParserTestUtils.executeWholeParsingPipeline("F2018C00645","allSops/F2018C00645.pdf")
  }

  test("cat 1B updates fail 2 - breast neoplasm ")
  {
    val r = ParserTestUtils.executeWholeParsingPipeline("F2018C00671","allSops/F2018C00671.pdf")

  }

  test("cat 1B updates fail 3 - breast neoplasm 2 ")
  {
    val r = ParserTestUtils.executeWholeParsingPipeline("F2018C00670","allSops/F2018C00670.pdf")
  }

  test("cat 1B updates fail 4 - cerebrovascular accident")
  {
    val r = ParserTestUtils.executeWholeParsingPipeline("F2018C00672","allSops/F2018C00672.pdf")
  }

  test("hypopituitarism factor categorisation")
  {
    val r = ParserTestUtils.executeWholeParsingPipeline("F2019L00009","allSops/F2019L00009.pdf")
  }

}
