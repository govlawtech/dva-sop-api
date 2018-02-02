package au.gov.dva.sopapi.tests.parsertests.subfactors

import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, Factor}
import au.gov.dva.sopapi.sopref.parsing.implementations.model.SubFactorInfo
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.paragraphReferenceSplitters.NewSoPStyleParaReferenceSplitter
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors.{NewSoPStyleSubFactorParser, OldSoPStyleSubFactorParser}
import com.google.common.collect.ImmutableSet
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SubfactorParserTests extends FunSuite {

  test("split to sub paras") {

    val underTest = new NewSoPStyleSubFactorParser()

    val result: List[SubFactorInfo] = underTest.divideFactorsToSubFactors(new mockFactor)
    result.foreach(println(_))
    assert(result.size == 5)
  }

  test("split para reference")
   {
     val testdata = "9(14)(b)"

     val underTest = new NewSoPStyleParaReferenceSplitter
     val result = underTest.trySplitParagraphReferenceToMainParagraphAndFirstLevelSubParagraph(testdata)
     assert(result._1 == "9(14)" && result._2 == "(b)")
   
   }

  test("Get subfactors from old style SoP section") {
    val underTest = new OldSoPStyleSubFactorParser()
    val mock = new mockOldStyleFactor
    val result = underTest.divideFactorsToSubFactors(mock)

  }


  test("Get condition variant") {
    val mock1 = new mockOldStyleFactor
    val underTest = new OldSoPStyleSubFactorParser
    val result = underTest.tryParseConditionVariant(mock1)
    println(result)
    assert (result.get == "intervertebral disc prolapse of the cervical spine")

  }

}


class mockOldStyleFactor extends  Factor {
  override def getParagraph: String = ???

  override def getText: String = "for intervertebral disc prolapse of the cervical spine only:\r\n(a) using a hand-held, vibrating, percussive, industrial tool for an\r\naverage of at least 25 hours per week, for a period of at least two\r\nyears within the ten years before the clinical onset of\r\nintervertebral disc prolapse; or\r\n(b) flying in high performance aircraft for a cumulative total of at\r\nleast 500 hours within any ten year period before the clinical\r\nonset of intervertebral disc prolapse\""

  override def getDefinedTerms: ImmutableSet[DefinedTerm] = ???
}


class mockFactor extends Factor
{
  override def getParagraph: String = "9(14)";

  override def getText: String = "for osteoarthritis of a joint of the lower limb only:\r\n(a) having:\r\n(i) an amputation involving either leg; or\r\n(ii) an asymmetric gait;\r\nfor at least three years before the clinical onset of osteoarthritis\r\nin that joint;\r\n(b) lifting loads of at least 20 kilograms while bearing weight\r\nthrough the affected joint to a cumulative total of at least\r\n100 000 kilograms within any ten year period before the clinical\r\nonset of osteoarthritis in that joint;\r\n(c) carrying loads of at least 20 kilograms while bearing weight\r\nthrough the affected joint to a cumulative total of at least 3 800\r\nhours within any ten year period before the clinical onset of\r\nosteoarthritis in that joint;\r\n(d) ascending or descending at least 150 stairs or rungs of a ladder\r\nper day, on more days than not, for a continuous period of at\r\nleast two years before the clinical onset of osteoarthritis in that\r\njoint; or\r\n(e) having increased bone mineral density before the clinical onset\r\nof osteoarthritis in that joint";

  override def getDefinedTerms: ImmutableSet[DefinedTerm] = ???
}
