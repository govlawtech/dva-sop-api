package au.gov.dva.sopapi.tests.parsertests.subfactors

import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, Factor}
import au.gov.dva.sopapi.sopref.parsing.implementations.model.SubFactorInfo
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors.NewSoPStyleSubFactorParser
import com.google.common.collect.ImmutableSet
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SubfactorParserTests extends FunSuite {

  val underTest = new NewSoPStyleSubFactorParser()

  val result: List[SubFactorInfo] = underTest.divideFactorsToSubFactors(new mockFactor)
  result.foreach(println(_))
  assert(result.size == 5)
}

class mockFactor extends Factor
{
  override def getParagraph: String = "9(14)";

  override def getText: String = "for osteoarthritis of a joint of the lower limb only:\r\n(a) having:\r\n(i) an amputation involving either leg; or\r\n(ii) an asymmetric gait;\r\nfor at least three years before the clinical onset of osteoarthritis\r\nin that joint;\r\n(b) lifting loads of at least 20 kilograms while bearing weight\r\nthrough the affected joint to a cumulative total of at least\r\n100 000 kilograms within any ten year period before the clinical\r\nonset of osteoarthritis in that joint;\r\n(c) carrying loads of at least 20 kilograms while bearing weight\r\nthrough the affected joint to a cumulative total of at least 3 800\r\nhours within any ten year period before the clinical onset of\r\nosteoarthritis in that joint;\r\n(d) ascending or descending at least 150 stairs or rungs of a ladder\r\nper day, on more days than not, for a continuous period of at\r\nleast two years before the clinical onset of osteoarthritis in that\r\njoint; or\r\n(e) having increased bone mineral density before the clinical onset\r\nof osteoarthritis in that joint";

  override def getDefinedTerms: ImmutableSet[DefinedTerm] = ???
}
