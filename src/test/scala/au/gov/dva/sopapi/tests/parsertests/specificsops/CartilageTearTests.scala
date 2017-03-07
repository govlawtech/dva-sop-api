
package au.gov.dva.sopapi.tests.parsertests.specificsops

import java.time.LocalDate

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.sopref.parsing.implementations.model.ParsedFactor
import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CartilageTearTests extends FunSuite {

  val rhFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2010L01666", "sops_rh/F2010L01666.pdf")
  }

  val bopFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2010L01667", "sops_bop/F2010L01667.pdf")
  }

  test("Parse entire RH acute articular cartilage tear SoP") {
    System.out.print(TestUtils.prettyPrint(StoredSop.toJson(rhFixture.result)))
    assert(rhFixture.result != null)
  }

  test("Parse RH acute articular cartilage tear register ID") {
    assert(rhFixture.result.getRegisterId === "F2010L01666")
  }

  test("Parse RH acute articular cartilage tear instrument number") {
    val instrumentNumber = rhFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 53)
    assert(instrumentNumber.getYear === 2010)
  }

  test("Parse RH acute articular cartilage tear citation") {
    assert(rhFixture.result.getCitation === "Statement of Principles concerning " +
      "acute articular cartilage tear No. 53 of 2010")
  }

  test("Parse RH acute articular cartilage tear condition name") {
    assert(rhFixture.result.getConditionName === "acute articular cartilage tear")
  }

  test("Parse RH acute articular cartilage tear effective from date") {
    assert(rhFixture.result.getEffectiveFromDate === LocalDate.of(2010, 6, 30))
  }

  test("Parse RH acute articular cartilage tear standard of proof") {
    assert(rhFixture.result.getStandardOfProof === StandardOfProof.ReasonableHypothesis)
  }

  // ICD codes
  test("Parse RH acute articular cartilage tear ICD codes") {
    assert(rhFixture.result.getICDCodes.isEmpty)
  }

  // Onset factors
  test("Parse RH acute articular cartilage tear onset factors") {
    val a = new ParsedFactor("6(a)",
      "having a significant physical force applied to or through the affected joint " +
        "at the time of the clinical onset of acute articular cartilage tear",
      Nil.toSet)

    val onsetFactors = rhFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 1)
    assert(onsetFactors.contains(a))
  }

  // Aggravation factors
  test("Parse RH acute articular cartilage tear aggravation factors") {
    val b = new ParsedFactor("6(b)",
      "inability to obtain appropriate clinical management for acute articular cartilage tear",
      Nil.toSet)

    val aggravationFactors = rhFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 1)
    assert(aggravationFactors.contains(b))
  }

  test("Parse entire BoP acute articular cartilage tear SoP") {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2010L01667", "sops_bop/F2010L01667.pdf")
    System.out.println(TestUtils.prettyPrint(StoredSop.toJson(result)))
    assert(result != null)
  }

  test("Parse BoP acute articular cartilage tear register ID") {
    assert(bopFixture.result.getRegisterId === "F2010L01667")
  }

  test("Parse BoP acute articular cartilage tear instrument number") {
    val instrumentNumber = bopFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 54)
    assert(instrumentNumber.getYear === 2010)
  }

  test("Parse BoP acute articular cartilage tear citation") {
    assert(bopFixture.result.getCitation === "Statement of Principles concerning " +
      "acute articular cartilage tear No. 54 of 2010")
  }

  test("Parse BoP acute articular cartilage tear condition name") {
    assert(bopFixture.result.getConditionName === "acute articular cartilage tear")
  }

  test("Parse BoP acute articular cartilage tear effective from date") {
    assert(bopFixture.result.getEffectiveFromDate === LocalDate.of(2010, 6, 30))
  }

  test("Parse BoP acute articular cartilage tear standard of proof") {
    assert(bopFixture.result.getStandardOfProof === StandardOfProof.BalanceOfProbabilities)
  }

  // ICD codes
  test("Parse BoP acute articular cartilage tear ICD codes") {
    assert(bopFixture.result.getICDCodes.isEmpty)
  }

  // Onset factors
  test("Parse BoP acute articular cartilage tear onset factors") {
    val a = new ParsedFactor("6(a)",
      "having a significant physical force applied to or through the affected joint at the " +
        "time of the clinical onset of acute articular cartilage tear",
      Nil.toSet)

    val onsetFactors = bopFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 1)
    assert(onsetFactors.contains(a))
  }

  // Aggravation factors
  test("Parse BoP acute articular cartilage tear aggravation factors") {
    val b = new ParsedFactor("6(b)",
      "inability to obtain appropriate clinical management for acute articular cartilage tear",
      Nil.toSet)

    val aggravationFactors = bopFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 1)
    assert(aggravationFactors.contains(b))
  }

}
