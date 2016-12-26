import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.sopref.parsing.implementations.LsSoPFactory
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.io.Source


@RunWith(classOf[JUnitRunner])
class LumbarSpondylosisTests extends FunSuite{



  test("Parse entire RH LS SoP") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2014L00933","sops\\rh\\F2014L00933.pdf")
    System.out.print(TestUtils.prettyPrint(StoredSop.toJson(result)))
    assert(result != null)
  }

  test("Parse entire BoP LS SoP")
  {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2014L00930","sops\\bop\\F2014L00930.pdf")
    System.out.println(TestUtils.prettyPrint(StoredSop.toJson(result)))
    assert(result != null)
  }


}
