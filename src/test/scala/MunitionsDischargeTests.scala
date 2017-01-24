
package au.gov.dva.sopapi.tests.parsers;

import java.time.LocalDate

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.sopref.parsing.implementations.model.ParsedFactor
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MunitionsDischargeTests extends FunSuite {

  val rhFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2012L01789", "sops_rh/F2012L01789.pdf")
  }

  val bopFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2012L01790", "sops_bop/F2012L01790.pdf")
  }

  test("Parse entire RH physical injury due to munitions discharge SoP") {
    System.out.print(TestUtils.prettyPrint(StoredSop.toJson(rhFixture.result)))
    assert(rhFixture.result != null)
  }

  test("Parse RH physical injury due to munitions discharge register ID") {
    assert(rhFixture.result.getRegisterId === "F2012L01789")
  }

  test("Parse RH physical injury due to munitions discharge instrument number") {
    assert(rhFixture.result.getInstrumentNumber.getNumber === 48)
    assert(rhFixture.result.getInstrumentNumber.getYear === 2012)
  }

  test("Parse RH physical injury due to munitions discharge citation") {
    assert(rhFixture.result.getCitation === "Statement of Principles concerning " +
      "physical injury due to munitions discharge No. 48 of 2012")
  }

  test("Parse RH physical injury due to munitions discharge condition name") {
    assert(rhFixture.result.getConditionName === "physical injury due to munitions discharge")
  }

  test("Parse RH physical injury due to munitions discharge effective from date") {
    assert(rhFixture.result.getEffectiveFromDate === LocalDate.of(2012, 9, 5))
  }

  test("Parse RH physical injury due to munitions discharge standard of proof") {
    assert(rhFixture.result.getStandardOfProof === StandardOfProof.ReasonableHypothesis)
  }

  // ICD codes
  test("Parse RH physical injury due to munitions discharge ICD codes") {
    assert(rhFixture.result.getICDCodes.isEmpty)
  }

  // Onset factors
  test("Parse RH physical injury due to munitions discharge onset factors") {
    val a = new ParsedFactor("6(a)",
      "sustaining physical injury due to munitions discharge",
      Nil, Nil.toSet)

    assert(rhFixture.result.getOnsetFactors.contains(a))
  }

  // Aggravation factors
  test("Parse RH physical injury due to munitions discharge aggravation factors") {
    val b = new ParsedFactor("6(b)",
      "inability to obtain appropriate clinical management for physical injury " +
        "due to munitions discharge",
      Nil, Nil.toSet)

    assert(rhFixture.result.getOnsetFactors.contains(b))
  }

  test("Parse entire BoP physical injury due to munitions discharge SoP") {
    System.out.println(TestUtils.prettyPrint(StoredSop.toJson(bopFixture.result)))
    assert(bopFixture.result != null)
  }

  test("Parse BoP physical injury due to munitions discharge register ID") {
    assert(bopFixture.result.getRegisterId === "F2012L01790")
  }

  test("Parse BoP physical injury due to munitions discharge instrument number") {
    assert(bopFixture.result.getInstrumentNumber.getNumber === 49)
    assert(bopFixture.result.getInstrumentNumber.getYear === 2012)
  }

  test("Parse BoP physical injury due to munitions discharge citation") {
    assert(bopFixture.result.getCitation === "Statement of Principles concerning " +
      "physical injury due to munitions discharge No. 49 of 2012")
  }

  test("Parse BoP physical injury due to munitions discharge condition name") {
    assert(bopFixture.result.getConditionName === "physical injury due to munitions discharge")
  }

  test("Parse BoP physical injury due to munitions discharge effective from date") {
    assert(bopFixture.result.getEffectiveFromDate === LocalDate.of(2012, 9, 5))
  }

  test("Parse BoP physical injury due to munitions discharge standard of proof") {
    assert(bopFixture.result.getStandardOfProof === StandardOfProof.BalanceOfProbabilities)
  }

  // ICD codes
  test("Parse BoP physical injury due to munitions discharge ICD codes") {
    assert(bopFixture.result.getICDCodes.isEmpty)
  }

  // Onset factors
  test("Parse BoP physical injury due to munitions discharge onset factors") {
    val a = new ParsedFactor("6(a)",
      "sustaining physical injury due to munitions discharge",
      Nil, Nil.toSet)

    assert(bopFixture.result.getOnsetFactors.contains(a))
  }

  // Aggravation factors
  test("Parse BoP physical injury due to munitions discharge aggravation factors") {
    val b = new ParsedFactor("6(b)",
      "inability to obtain appropriate clinical management for physical injury due " +
        "to munitions discharge",
      Nil, Nil.toSet)

    assert(bopFixture.result.getAggravationFactors.contains(b))
  }

}
