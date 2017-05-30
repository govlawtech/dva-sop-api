package au.gov.dva.sopapi.tests.parsertests

import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.traits.PreAug2015FactorsParser
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class SingleFactorParsingTests extends FunSuite {

  object factorsParserUnderTest extends PreAug2015FactorsParser


  test("Split sub paras") {

    val input = "(i) having an amputation involving either leg; or\r\n(ii) having an asymmetric gait;\r\nfor at least three years before the clinical worsening of\r\nosteoarthritis in that joint; or"
    val result = SoPExtractorUtilities.splitFactorToSubFactors(input.split("[\r\n]+").toList)
    assert(result.size == 2 && result(1).size == 3)
  }

  test("Split last sub para to sub para and tail with semi colon end")
  {
    val input = "(ii) having an asymmetric gait;\nfor at least three years before the clinical worsening of\nosteoarthritis in that joint; or"
    val result = SoPExtractorUtilities.splitOutTailIfAny(input)
    assert(result._2.isDefined && result._1.startsWith("(ii)") && result._2.get.endsWith("; or"))
  }

  test("Do not split last sub para to sub para and tail with comma end")
  {
    val input = "(ii) having an asymmetric gait,\nfor at least three years before the clinical worsening of\nosteoarthritis in that joint; or"
    val result = SoPExtractorUtilities.splitOutTailIfAny(input)
    assert(result._2.isEmpty && result._1.size == input.size)
  }




  test("Split head from rest for individual factor") {
    val input = "(b) for central sleep apnoea only,\n(i) having congestive cardiac failure at the time of the clinical\nonset of sleep apnoea; or\n(ii) using a long-acting opioid at an average daily morphine\nequivalent dose of at least 75 milligrams for at least the two\nmonths before the clinical onset of sleep apnoea; or"
    val inputSplitToLines = input.split("[\r\n]+").toList
    val result = SoPExtractorUtilities.splitFactorToHeaderAndRest(inputSplitToLines)
    assert(!result._2.isEmpty)
  }




  test("Split tail from last factor") {
     val lastFactor = "(ii) having an asymmetric gait;\nfor at least three years before the clinical worsening of\nosteoarthritis in that joint; or";
      val result = SoPExtractorUtilities.splitOutTailIfAny(lastFactor)

    println("PARA: " + result._1)
    println("TAIL: " +  result._2.get)
  }


}
