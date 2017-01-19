
package au.gov.dva.sopapi.tests.parsers;

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OsteoarthritisTests extends FunSuite {

  ignore("Parse entire RH LS SoP") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2011C00491", "sops_rh/F2011C00491.pdf")
    System.out.print(TestUtils.prettyPrint(StoredSop.toJson(result)))
    assert(result != null)
  }

  ignore("Parse entire BoP LS SoP")
  {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2011C00492", "sops_bop/F2011C00492.pdf")
    System.out.println(TestUtils.prettyPrint(StoredSop.toJson(result)))
    assert(result != null)
  }

}
