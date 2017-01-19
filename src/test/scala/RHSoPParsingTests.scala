package au.gov.dva.sopapi.tests.parsers

import java.io.PrintWriter

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import net.engio.mbassy.bus.config.IBusConfiguration.Properties
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class RHSoPParsingTests extends FunSuite {

  ignore("Parse all RH SoPs") {
    val rhIds = ParserTestUtils.resourceToString("rhSopRegisterIds.txt").split(scala.util.Properties.lineSeparator);

    val errorMap = mutable.HashMap.empty[String, Throwable];
    val passedList = mutable.MutableList.empty[String];

    for (rhId <- rhIds) {

      try {
        val result = ParserTestUtils.executeWholeParsingPipeline(rhId, "sops_rh/" + rhId + ".pdf")

        if (result == null) {
          errorMap += (rhId -> null)
        } else {
          passedList += rhId
        }
      } catch {
        case e: Throwable => errorMap += (rhId -> e)
      }

    }

    val pw = new PrintWriter("rhParseResults.txt")

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

    if (!errorMap.isEmpty) {
      fail("Parse failures: " + errorMap.keySet.mkString(","))
    }
  }

}
