package au.gov.dva.sopapi.tests.parsertests

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.util.Properties

@RunWith(classOf[JUnitRunner])
class AllSopsTest extends FunSuite{

  private val specs = loadSpecs

  private def loadSpecs = {
    val csvText = ParserTestUtils.resourceToString("sops.csv")
    // name, bop, rh
    val specs : List[(String,String,String)] = csvText.split(Properties.lineSeparator)
        .drop(1)
        .map(l => l.split(",")).toList
        .map(i => (i(0).trim,
          getIdfromUrl(i(1).trim),
          getIdfromUrl(i(2).trim)))
    specs
  }

  private def getIdfromUrl(urlString : String) = {
    val m = """F[0-9A-Z]+$""".r.findFirstIn(urlString)
    assert(m.isDefined)
    m.get
  }

  private def testSoPPair(name: String, bopId: String, rhId: String) = {

    val boPResult = testSingleSoP(name,bopId)
    val rhResult = testSingleSoP(name,rhId)
    (boPResult,rhResult)
  }


  private def testSingleSoP(name : String, id : String) : (String,String,Boolean,Option[SoP], Option[String]) = {
    try {
      val result: SoP = ParserTestUtils.executeWholeParsingPipeline(id, "allSops/" + id + ".pdf")

      (name,id,true,Some(result),None)
    }
    catch {

      case e :  Throwable => {
        println("ERROR PARSING: " + name + ", " + id)
        // e.printStackTrace()
        (name, id, false, None,Some(e.getMessage.take(512).mkString("")))
      }
    }
  }

  test("Parse all known sops")
  {
    val allCurrentRegisterIds = ParserTestUtils.resourceToString("allSops/allKnownSops.txt").split(Properties.lineSeparator).toList
    val fails = allCurrentRegisterIds
      .map(id => testSingleSoP(id,id))
      .filter(r => !r._3)

    fails.foreach(f => println(f))

    println("FAILS COUNT: " + fails.size)
  }

}
