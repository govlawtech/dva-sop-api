package au.gov.dva.sopapi.tests.parsertests

import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.traits.PreAug2015FactorsParser
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.util.Properties


@RunWith(classOf[JUnitRunner])
class SingleFactorParsingTests extends FunSuite {

  object factorsParserUnderTest extends PreAug2015FactorsParser


  test("Split sub paras") {

    val input = "(i) having an amputation involving either leg; or" + Properties.lineSeparator + "(ii) having an asymmetric gait;" + Properties.lineSeparator + "for at least three years before the clinical worsening of" + Properties.lineSeparator + "osteoarthritis in that joint; or"
    val result = SoPExtractorUtilities.splitFactorToSubFactors(input.split("[" + Properties.lineSeparator + "]+").toList)
    assert(result.size == 2 && result(1).size == 3)
  }

  test("Split last sub para to sub para and tail with semi colon end")
  {
    val input = "(ii) having an asymmetric gait;" + Properties.lineSeparator + "for at least three years before the clinical worsening of" + Properties.lineSeparator + "osteoarthritis in that joint; or"
    val result = SoPExtractorUtilities.splitOutTailIfAny(input)
    assert(result._2.isDefined && result._1.startsWith("(ii)") && result._2.get.endsWith("; or"))
  }

  test("Do not split last sub para to sub para and tail with comma end")
  {
    val input = "(ii) having an asymmetric gait," + Properties.lineSeparator + "for at least three years before the clinical worsening of" + Properties.lineSeparator + "osteoarthritis in that joint; or"
    val result = SoPExtractorUtilities.splitOutTailIfAny(input)
    assert(result._2.isEmpty && result._1.size == input.size)
  }

  test("Parse single level para") {

    val input = "(r) having a disorder associated with loss of pain sensation or proprioception involving the affected joint before the clinical onset of osteoarthritis in that joint; or"
    val result = factorsParserUnderTest.parseSingleFactor(input)
    println(result)

  }

  test("Parse two level para") {
    val input ="(h) for obstructive sleep apnoea only," + Properties.lineSeparator + "(i) having chronic obstruction or chronic narrowing of the upper" + Properties.lineSeparator + "airway at the time of the clinical worsening of sleep apnoea; or" + Properties.lineSeparator + "(ii) being obese at the time of the clinical worsening of sleep" + Properties.lineSeparator + "apnoea; or" + Properties.lineSeparator + "(iii) having hypothyroidism at the time of the clinical worsening of" + Properties.lineSeparator + "sleep apnoea; or" + Properties.lineSeparator + "(iv) having acromegaly at the time of the clinical worsening of sleep" + Properties.lineSeparator + "apnoea; or" + Properties.lineSeparator + "(v) being treated with antiretroviral therapy for human" + Properties.lineSeparator + "immunodeficiency virus infection before the clinical worsening" + Properties.lineSeparator + "of sleep apnoea; or"
    val result = factorsParserUnderTest.parseSingleFactor(input)
    println(result)
  }

  test("Parse two level para with tail") {
    val input = "(ee) for osteoarthritis of a joint of the lower limb only," + Properties.lineSeparator + "(i) having an amputation involving either leg; or" + Properties.lineSeparator + "(ii) having an asymmetric gait;" + Properties.lineSeparator + "for at least three years before the clinical worsening of" + Properties.lineSeparator + "osteoarthritis in that joint; or"
    val result = factorsParserUnderTest.parseSingleFactor(input)
    println(result)
  }


  test("Split head from rest for individual factor") {
    val input = "(b) for central sleep apnoea only," + Properties.lineSeparator + "(i) having congestive cardiac failure at the time of the clinical" + Properties.lineSeparator + "onset of sleep apnoea; or" + Properties.lineSeparator + "(ii) using a long-acting opioid at an average daily morphine" + Properties.lineSeparator + "equivalent dose of at least 75 milligrams for at least the two" + Properties.lineSeparator + "months before the clinical onset of sleep apnoea; or"
    val inputSplitToLines = input.split(scala.util.Properties.lineSeparator).toList
    val result = SoPExtractorUtilities.splitFactorToHeaderAndRest(inputSplitToLines)
    assert(!result._2.isEmpty)
  }

  test("Parse single factor from text incl line breaks without tail") {

    val input = "(b) for central sleep apnoea only," + Properties.lineSeparator + "(i) having congestive cardiac failure at the time of the clinical" + Properties.lineSeparator + "onset of sleep apnoea; or" + Properties.lineSeparator + "(ii) using a long-acting opioid at an average daily morphine" + Properties.lineSeparator + "equivalent dose of at least 75 milligrams for at least the two" + Properties.lineSeparator + "months before the clinical onset of sleep apnoea; or"
    val result = factorsParserUnderTest.parseSingleFactor(input)
    println(result)

  }

  test("Parse single factor from text incl line breaks and with tail") {
    val input = "(ee) for osteoarthritis of a joint of the lower limb only," + Properties.lineSeparator + "(i) having an amputation involving either leg; or" + Properties.lineSeparator + "(ii) having an asymmetric gait;" + Properties.lineSeparator + "for at least three years before the clinical worsening of" + Properties.lineSeparator + "osteoarthritis in that joint; or"
    val result = factorsParserUnderTest.parseSingleFactor(input)
    println(result)
  }




  test("Split tail from last factor") {
     val lastFactor = "(ii) having an asymmetric gait;" + Properties.lineSeparator + "for at least three years before the clinical worsening of" + Properties.lineSeparator + "osteoarthritis in that joint; or";
      val result = SoPExtractorUtilities.splitOutTailIfAny(lastFactor)

    println("PARA: " + result._1)
    println("TAIL: " +  result._2.get)
  }


}
