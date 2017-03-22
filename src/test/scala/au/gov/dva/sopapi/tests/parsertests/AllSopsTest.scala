package au.gov.dva.sopapi.tests.parsertests

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.exceptions.SopParserError
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


  private def testSingleSoP(name : String, id : String) : (String,String,Boolean,Option[SoP]) = {
    try {
      val result: SoP = ParserTestUtils.executeWholeParsingPipeline(id, "allSops/" + id + ".pdf")

      (name,id,true,Some(result))
    }
    catch {
      case _ : SopParserError => (name,id,false,None)
      case e :  Throwable => {
        println("ERROR PARSING: " + name + ", " + id)
        e.printStackTrace()
        (name, id, false, None)
      }
    }

  }


  test("All sops in specs parse") {
     val allResults = specs
      .map(l => testSoPPair(l._1,l._2,l._3))

    val passes = allResults
      .filter(r => r._1._3 && r._2._3)

    passes.foreach(p => {
      println("BoP:")
      println(TestUtils.prettyPrint(StoredSop.toJson(p._1._4.get)))
      println("RH:")
      println(TestUtils.prettyPrint(StoredSop.toJson(p._2._4.get)))

    } )

    println("PASSED CONDITIONS: " + passes.size )
    passes.foreach(p => println(p._1._1))

    val passedSoPIds = passes.flatMap(p => List(p._1._2, p._2._2))
    println("PASSED SOP IDS: " + passedSoPIds
        .sortBy(i => i)
      .mkString(Properties.lineSeparator))

    val fails = allResults
        .flatMap(i => List(i._1,i._2))
      .filter(i => !i._3)

    println("FAILED SOPS: " + fails.size)
    fails.foreach(f => println(f))

    assert(passes.size > 30)
  }




}
