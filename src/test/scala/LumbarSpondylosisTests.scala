
package au.gov.dva.sopapi.tests.parsers;


import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.sopref.data.Conversions
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.sopref.parsing.factories.SoPFactoryLocator
import au.gov.dva.sopapi.sopref.parsing.implementations.GenericClenser
import org.junit.Ignore
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner



@RunWith(classOf[JUnitRunner])
class LumbarSpondylosisTests extends FunSuite {

  test("Parse entire RH LS SoP") {
      val result = ParserTestUtils.executeWholeParsingPipeline("F2014L00933", "sops_rh/F2014L00933.pdf")
      System.out.print(TestUtils.prettyPrint(StoredSop.toJson(result)))
      assert(result != null)
  }

  test("Parse entire BoP LS SoP")
  {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2014L00930", "sops_bop/F2014L00930.pdf")
    System.out.println(TestUtils.prettyPrint(StoredSop.toJson(result)))
    assert(result != null)
  }



}
