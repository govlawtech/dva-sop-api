package au.gov.dva.sopapi.tests.parsertests

import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.traits.FactorsParser
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class SingleFactorParsingTests extends FunSuite {

  object factorsParserUnderTest extends FactorsParser


  test("Split sub paras") {

    val input = "(i) having an amputation involving either leg; or\n(ii) having an asymmetric gait;\nfor at least three years before the clinical worsening of\nosteoarthritis in that joint; or"
    val result = SoPExtractorUtilities.splitFactorToSubFactors(input.split("[\r\n]+").toList)
    assert(result.size == 2 && result(1).size == 3)
  }

  test("Split last sub para to sub para and tail with semi colon end")
  {
    val input = "(ii) having an asymmetric gait;\nfor at least three years before the clinical worsening of\nosteoarthritis in that joint; or"
    val result = SoPExtractorUtilities.splitOutTailIfAny(input)
    assert(result._2.isDefined && result._1.size + result._2.get.size == input.size)
  }

  test("Do not split last sub para to sub para and tail with comma end")
  {
    val input = "(ii) having an asymmetric gait,\nfor at least three years before the clinical worsening of\nosteoarthritis in that joint; or"
    val result = SoPExtractorUtilities.splitOutTailIfAny(input)
    assert(result._2.isEmpty && result._1.size == input.size)
  }

  test("Parse single level para") {

    val input = "(r) having a disorder associated with loss of pain sensation or proprioception involving the affected joint before the clinical onset of osteoarthritis in that joint; or"
    val result = factorsParserUnderTest.parseSingleFactor(input)
    println(result)

  }

  test("Parse two level para") {
    val input ="(h) for obstructive sleep apnoea only,\n(i) having chronic obstruction or chronic narrowing of the upper\nairway at the time of the clinical worsening of sleep apnoea; or\n(ii) being obese at the time of the clinical worsening of sleep\napnoea; or\n(iii) having hypothyroidism at the time of the clinical worsening of\nsleep apnoea; or\n(iv) having acromegaly at the time of the clinical worsening of sleep\napnoea; or\n(v) being treated with antiretroviral therapy for human\nimmunodeficiency virus infection before the clinical worsening\nof sleep apnoea; or"
    val result = factorsParserUnderTest.parseSingleFactor(input)
    println(result)
  }

  test("Parse two level para with tail") {
    val input = "(ee) for osteoarthritis of a joint of the lower limb only,\n(i) having an amputation involving either leg; or\n(ii) having an asymmetric gait;\nfor at least three years before the clinical worsening of\nosteoarthritis in that joint; or"
    val result = factorsParserUnderTest.parseSingleFactor(input)
    println(result)
  }


  test("Split head from rest for individual factor") {
    val input = "(b) for central sleep apnoea only,\n(i) having congestive cardiac failure at the time of the clinical\nonset of sleep apnoea; or\n(ii) using a long-acting opioid at an average daily morphine\nequivalent dose of at least 75 milligrams for at least the two\nmonths before the clinical onset of sleep apnoea; or"
    val inputSplitToLines = input.split("[\r\n]+").toList
    val result = SoPExtractorUtilities.splitFactorToHeaderAndRest(inputSplitToLines)
    assert(!result._2.isEmpty)
  }

  test("Parse single factor from text incl line breaks without tail") {

    val input = "(b) for central sleep apnoea only,\n(i) having congestive cardiac failure at the time of the clinical\nonset of sleep apnoea; or\n(ii) using a long-acting opioid at an average daily morphine\nequivalent dose of at least 75 milligrams for at least the two\nmonths before the clinical onset of sleep apnoea; or"
    val result = factorsParserUnderTest.parseSingleFactor(input)
    println(result)

  }

  test("Parse single factor from text incl line breaks and with tail") {
    val input = "(ee) for osteoarthritis of a joint of the lower limb only,\n(i) having an amputation involving either leg; or\n(ii) having an asymmetric gait;\nfor at least three years before the clinical worsening of\nosteoarthritis in that joint; or"
    val result = factorsParserUnderTest.parseSingleFactor(input)
    println(result)
  }




  test("Split tail from last factor") {
     val lastFactor = "(ii) having an asymmetric gait;\nfor at least three years before the clinical worsening of\nosteoarthritis in that joint; or";
      val result = SoPExtractorUtilities.splitOutTailIfAny(lastFactor)

    println("PARA: " + result._1)
    println("TAIL: " +  result._2.get)
  }


}
