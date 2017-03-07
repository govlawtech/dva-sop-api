package au.gov.dva.sopapi.tests.parsertests

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PreAug2015Tests extends FunSuite{

    val BoPidsForPreAug2015 = List(
      "F2014L00932",
      "F2010L02319",
      "F2014L01786",
      "F2008L02195",
      "F2010L01051",
      "F2013L00021",
      "F2010L01665",
      "F2011L00766",
      "F2010L01049",
      "F2014L00930",
      "F2014L01145",
      "F2013L01133",
      "F2014L00929",
      "F2012L01364")

   val RHIdsForPreAug2015 = List(
      "F2014L00928",
      "F2010L02318",
      "F2008L02192",
      "F2010L01050",
      "F2013L00020",
      "F2010L01664",
      "F2011L00783",
      "F2010L01048",
      "F2014L00933",
      "F2011L01743",
      "F2014L01144",
      "F2013L01129",
      "F2014L00931",
      "F2012L00016",
      "F2012L01361"
    )

    test("Push all pre Aug BOP 2015 through pipeline")
     {

        val parseResults: List[SoP] =  BoPidsForPreAug2015.map(id => {
          ParserTestUtils.executeWholeParsingPipeline(id, "sops_bop/" + id + ".pdf")
        })

        parseResults.foreach(s => println(TestUtils.prettyPrint(StoredSop.toJson(s))))

     }

  test("Push all pre Aug  2015 RH through pipeline")
  {

    val parseResults = RHIdsForPreAug2015.map(id => {
      ParserTestUtils.executeWholeParsingPipeline(id, "sops_rh/" + id + ".pdf")

    })

    assert(parseResults.size == 15)

  }






}
