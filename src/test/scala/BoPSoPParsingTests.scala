package au.gov.dva.sopapi.tests.parsers

import java.io.PrintWriter

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable
import scala.util.Properties

@RunWith(classOf[JUnitRunner])
class BoPSoPParsingTests extends FunSuite {

  ignore("Parse all BoP SoPs") {
    val rhIds = ParserTestUtils.resourceToString("bopSopRegisterIds.txt").split(Properties.lineSeparator);

    val errorMap = mutable.HashMap.empty[String, Throwable];
    val passedList = mutable.MutableList.empty[String];

    for (rhId <- rhIds) {

      try {
        val result = ParserTestUtils.executeWholeParsingPipeline(rhId, "sops_bop/" + rhId + ".pdf")

        if (result == null) {
          errorMap += (rhId -> null)
        } else {
          passedList += rhId
        }
      } catch {
        case e: Throwable => errorMap += (rhId -> e)
      }
    }

    val pw = new PrintWriter("bopParseResults.txt")

    for (rhId <- passedList) {
      System.out.println("PASSED " + rhId);
      pw.println("PASSED " + rhId);
    }

    for (rhId <- errorMap.keySet) {
      System.out.println("FAILED " + rhId);
      pw.println("FAILED " + rhId)
      errorMap(rhId).printStackTrace(pw)
      pw.println()
    }

    for (rhId <- passedList) {
      System.out.println(rhId);
    }

    if (!errorMap.isEmpty) {
      fail("Parse failures: " + errorMap.keySet.mkString(","))
    }
  }

}
