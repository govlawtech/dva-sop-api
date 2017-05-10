
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
class LabralTearTests extends FunSuite {

  val rhFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2010L02850", "sops_rh/F2010L02850.pdf")
  }

  val bopFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2010L02851", "sops_bop/F2010L02851.pdf")
  }

  test("Parse entire RH labral tear SoP") {
    System.out.print(TestUtils.prettyPrint(StoredSop.toJson(rhFixture.result)))
    assert(rhFixture.result != null)
  }

  test("Parse RH labral tear register ID") {
    assert(rhFixture.result.getRegisterId === "F2010L02850")
  }

  test("Parse RH labral tear instrument number") {
    val instrumentNumber = rhFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 94)
    assert(instrumentNumber.getYear === 2010)
  }

  test("Parse RH labral tear citation") {
    assert(rhFixture.result.getCitation === "Statement of Principles concerning " +
      "labral tear No. 94 of 2010")
  }

  test("Parse RH labral tear condition name") {
    assert(rhFixture.result.getConditionName === "labral tear")
  }

  test("Parse RH labral tear effective from date") {
    assert(rhFixture.result.getEffectiveFromDate === LocalDate.of(2010, 11, 10))
  }

  test("Parse RH labral tear standard of proof") {
    assert(rhFixture.result.getStandardOfProof === StandardOfProof.ReasonableHypothesis)
  }

  // ICD codes
  test("Parse RH labral tear ICD codes") {
    assert(rhFixture.result.getICDCodes.isEmpty)
  }

  // Onset factors
  test("Parse RH labral tear onset factors") {
    val a = new ParsedFactor("6(a)",
      "having a significant physical force applied to or through the affected shoulder " +
        "joint or the affected hip joint at the time of the clinical onset of labral tear",
      Nil.toSet)

    val b = new ParsedFactor("6(b)",
      "for labral tear of the shoulder joint only, performing forceful and repetitive " +
        "throwing motions or forceful and repetitive overhead motions of the arm of the " +
        "affected side for at least eight hours per week for the one month before the " +
        "clinical onset of labral tear",
      Nil.toSet)

    val onsetFactors = rhFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 2)
    assert(onsetFactors.contains(a))
    assert(onsetFactors.contains(b))
  }

  // Aggravation factors
  test("Parse RH labral tear aggravation factors") {
    val c = new ParsedFactor("6(c)",
      "inability to obtain appropriate clinical management for labral tear",
      Nil.toSet)

    val aggravationFactors = rhFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 1)
    assert(aggravationFactors.contains(c))
  }

  test("Parse entire BoP labral tear SoP") {
    System.out.println(TestUtils.prettyPrint(StoredSop.toJson(bopFixture.result)))
    assert(bopFixture.result != null)
  }

  test("Parse BoP labral tear register ID") {
    assert(bopFixture.result.getRegisterId === "F2010L02851")
  }

  test("Parse BoP labral tear instrument number") {
    val instrumentNumber = bopFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 95)
    assert(instrumentNumber.getYear === 2010)
  }

  test("Parse BoP labral tear citation") {
    assert(bopFixture.result.getCitation === "Statement of Principles concerning " +
      "labral tear No. 95 of 2010")
  }

  test("Parse BoP labral tear condition name") {
    assert(bopFixture.result.getConditionName === "labral tear")
  }

  test("Parse BoP labral tear effective from date") {
    assert(bopFixture.result.getEffectiveFromDate === LocalDate.of(2010, 11, 10))
  }

  test("Parse BoP labral tear standard of proof") {
    assert(bopFixture.result.getStandardOfProof === StandardOfProof.BalanceOfProbabilities)
  }

  // ICD codes
  test("Parse BoP labral tear ICD codes") {
    assert(bopFixture.result.getICDCodes.isEmpty)
  }

  // Onset factors
  test("Parse BoP labral tear onset factors") {
    val a = new ParsedFactor("6(a)",
      "having a significant physical force applied to or through the affected shoulder " +
        "joint or the affected hip joint at the time of the clinical onset of labral tear",
      Nil.toSet)

    val b = new ParsedFactor("6(b)",
      "for labral tear of the shoulder joint only, performing forceful and repetitive " +
        "throwing motions or forceful and repetitive overhead motions of the arm of the " +
        "affected side for at least eight hours per week for the one month before the " +
        "clinical onset of labral tear",
      Nil.toSet)

    val onsetFactors = bopFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 2)
    assert(onsetFactors.contains(a))
    assert(onsetFactors.contains(b))
  }

  // Aggravation factors
  test("Parse BoP labral tear aggravation factors") {
    val c = new ParsedFactor("6(c)",
      "inability to obtain appropriate clinical management for labral tear",
      Nil.toSet)

    val aggravationFactors = bopFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 1)
    assert(aggravationFactors.contains(c))
  }

}
