package au.gov.dva.sopapi.tests.parsertests.subfactors

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, Factor, SoP}
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, SubFactorInfo}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.paragraphReferenceSplitters.NewSoPStyleParaReferenceSplitter
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors.{NewStyleSubFactorParser, OldStyleSubfactorParser}
import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import com.google.common.collect.ImmutableSet
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class SubfactorParserTests extends FunSuite {

  val newStyleParser = new NewStyleSubFactorParser()
  val oldStyleParser = new OldStyleSubfactorParser()
  test("split to sub paras") {


    val result: List[SubFactorInfo] = newStyleParser.divideToSubfactors(new mockFactor)
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
    val underTest = newStyleParser
    val mock = new mockOldStyleFactor
    val result = underTest.divideToSubfactors(mock)
  }


  test("Get condition variant") {
    val mock1 = new mockOldStyleFactor
    val underTest = newStyleParser
    val result = underTest.tryParseConditionVariant(mock1)
    println(result)
    assert (result.get == "intervertebral disc prolapse of the cervical spine")
  }

  test("Parse entire osteoarthritis SoP to check for condition variants")
   {
     val r: SoP = ParserTestUtils.executeWholeParsingPipeline("F2019C00582","allSops/F2019C00582.pdf")

     val conditionVariants = (r.getOnsetFactors.asScala).filter(f => f.getConditionVariant.isPresent)
       .map(f => (f.getConditionVariant.get().getName,f.getText))

    println(conditionVariants.mkString("\n"))
    assert (conditionVariants.size == 7)


    println(TestUtils.prettyPrint(StoredSop.toJson(r)))
   }

  test("Parse entire ischaemic heart disease SoP to check for condition variants")
  {
    val r: SoP = ParserTestUtils.executeWholeParsingPipeline("F2018C00631","allSops/F2018C00631.pdf")
     val conditionVariants = (r.getOnsetFactors.asScala).filter(f => f.getConditionVariant.isPresent)
       .map(f => (f.getConditionVariant.get().getName,f.getText))

    assert (conditionVariants.size == 1)
    println(TestUtils.prettyPrint(StoredSop.toJson(r)))
  }


  test("Parse old style diabetes SoP for condition variants")
  {
     val r: SoP = ParserTestUtils.executeWholeParsingPipeline("F2016C00252","allSops/F2016C00252.pdf")
     val conditionVariants = (r.getOnsetFactors.asScala).filter(f => f.getConditionVariant.isPresent)
       .map(f => (f.getConditionVariant.get().getName,f.getText))

    println(conditionVariants.mkString("\n"))
    println(TestUtils.prettyPrint(StoredSop.toJson(r)))
  }

}


class mockOldStyleFactor extends  FactorInfo {


  override def getText: String = "for intervertebral disc prolapse of the cervical spine only:\r\n(a) using a hand-held, vibrating, percussive, industrial tool for an\r\naverage of at least 25 hours per week, for a period of at least two\r\nyears within the ten years before the clinical onset of\r\nintervertebral disc prolapse; or\r\n(b) flying in high performance aircraft for a cumulative total of at\r\nleast 500 hours within any ten year period before the clinical\r\nonset of intervertebral disc prolapse\""

  override def getLetter: String = ???
}


class mockFactor extends FactorInfo
{
  override def getLetter: String = "9(14)";
  override def getText: String = "for osteoarthritis of a joint of the lower limb only:\r\n(a) having:\r\n(i) an amputation involving either leg; or\r\n(ii) an asymmetric gait;\r\nfor at least three years before the clinical onset of osteoarthritis\r\nin that joint;\r\n(b) lifting loads of at least 20 kilograms while bearing weight\r\nthrough the affected joint to a cumulative total of at least\r\n100 000 kilograms within any ten year period before the clinical\r\nonset of osteoarthritis in that joint;\r\n(c) carrying loads of at least 20 kilograms while bearing weight\r\nthrough the affected joint to a cumulative total of at least 3 800\r\nhours within any ten year period before the clinical onset of\r\nosteoarthritis in that joint;\r\n(d) ascending or descending at least 150 stairs or rungs of a ladder\r\nper day, on more days than not, for a continuous period of at\r\nleast two years before the clinical onset of osteoarthritis in that\r\njoint; or\r\n(e) having increased bone mineral density before the clinical onset\r\nof osteoarthritis in that joint";

}
