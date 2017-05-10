package au.gov.dva.sopapi.tests.parsertests

import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.LegalReferenceSequencesPostAug2015
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LegalReferenceSequenceTests extends FunSuite {

  test("Post Aug 2015 ordinary sequence correctly returns false")
  {
    val testData = List("(1)","(2)","(3)","(4)","(6)") // sec five repealed
    val result = LegalReferenceSequencesPostAug2015.isNextMainFactorLine(Some("(3)"),"(2)")
    assert(result == false)

  }

  test("Post Aug 2015 ordinary sequence correctly returns true")
  {
    val testData = List("(1)","(2)","(3)","(4)","(6)") // sec five repealed
  val result = LegalReferenceSequencesPostAug2015.isNextMainFactorLine(Some("(4)"),"(6)")
    assert(result == true)
  }

  test("Int to small romans")
  {
    val testData = 1 to 40
    val result = testData.map(LegalReferenceSequencesPostAug2015.intToSmallRoman)
    println(result)
  }


}
