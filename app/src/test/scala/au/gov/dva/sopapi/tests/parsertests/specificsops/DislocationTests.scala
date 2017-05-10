
package au.gov.dva.sopapi.tests.parsertests.specificsops

import java.time.LocalDate

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.sopref.data.sops.{BasicICDCode, StoredSop}
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{ParsedDefinedTerm, ParsedFactor}
import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DislocationTests extends FunSuite {

  val rhFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2010L01040", "sops_rh/F2010L01040.pdf")
  }

  val bopFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2010L01041", "sops_bop/F2010L01041.pdf")
  }

  val biomechanicalDef = new ParsedDefinedTerm("a biomechanical abnormality involving " +
    "the affected joint",
    "means\nan abnormality of the forces acting on the affected joint as a result of a\n" +
      "muscle, tendon, ligament, or bone, that maintains the normal structural\nor " +
      "functional relationship between the articulating surfaces of the\naffected joint, " +
      "and that is not functioning correctly, is abnormal or is\nmisaligned. This " +
      "definition includes biomechanical abnormality as a\nresult of surgery involving " +
      "the stabilising structures of the affected joint\nand extra-articular malunion of " +
      "a fracture of a bone involved in the\naffected joint")

  val diseaseProcessDef = new ParsedDefinedTerm("a disease process affecting the normal " +
    "structural or functional relationship between the articulating surfaces of the affected joint",
    "means:\n(a) a degenerative or inflammatory condition of the affected joint,\nincluding " +
      "neuropathic arthropathy, rheumatoid arthritis,\nosteoarthritis or tuberculosis, " +
      "which affects the integrity of the\naffected joint; or\n(b) a neurological, muscular, " +
      "or vascular condition, including\ncerebrovascular accident, epileptic seizure, " +
      "poliomyelitis or\ndyskinesia caused by neuroleptic drugs, which affects those\ntissues, " +
      "or control of those tissues, which maintain the integrity of\nthe affected joint")

  val softTissueDef = new ParsedDefinedTerm("a soft tissue structure as specified",
    "means a tendon, ligament or\nfibrocartilaginous structure that contributes to joint " +
      "stability in the\naffected joint")

  val wideOpeningDef = new ParsedDefinedTerm("an activity that involves wide opening of the mouth",
    "means an\nactivity in which an active force imposes undue tension on the\n" +
      "temporomandibular joint capsular ligaments, and includes dental\nprocedures, " +
      "vomiting and coughing")

  val neuropathicDef = new ParsedDefinedTerm("neuropathic arthropathy",
    "means a progressive destructive arthritis\nassociated with loss of pain sensation " +
      "or proprioception, which can\nresult from various underlying disorders, including " +
      "tabes dorsalis,\nsyringomyelia, and diabetes mellitus")

  val traumaDef = new ParsedDefinedTerm("physical trauma to the affected joint",
    "means a force applied directly\nto the affected joint, or to the body and which is " +
      "transmitted to the\naffected joint. This definition includes electric shock")

  test("Parse entire RH dislocation SoP") {
    System.out.print(TestUtils.prettyPrint(StoredSop.toJson(rhFixture.result)))
    assert(rhFixture.result != null)
  }

  test("Parse RH dislocation register ID") {
    assert(rhFixture.result.getRegisterId === "F2010L01040")
  }

  test("Parse RH dislocation instrument number") {
    val instrumentNumber = rhFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 24)
    assert(instrumentNumber.getYear === 2010)
  }

  test("Parse RH dislocation citation") {
    assert(rhFixture.result.getCitation === "Statement of Principles concerning " +
      "dislocation No. 24 of 2010")
  }

  test("Parse RH dislocation condition name") {
    assert(rhFixture.result.getConditionName === "dislocation")
  }

  test("Parse RH dislocation effective from date") {
    assert(rhFixture.result.getEffectiveFromDate === LocalDate.of(2010, 5, 12))
  }

  test("Parse RH dislocation standard of proof") {
    assert(rhFixture.result.getStandardOfProof === StandardOfProof.ReasonableHypothesis)
  }

  // ICD codes
  test("Parse RH dislocation ICD codes") {
    val icdCodes = rhFixture.result.getICDCodes
    assert(icdCodes.size() === 17)
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M24.3")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M99.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S03.0")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S33.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S33.2")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S33.3")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S43.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S43.2")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S43.3")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S53.0")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S53.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S63.0")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S63.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S63.2")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S93.0")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S93.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S93.3")))
  }

  // Onset factors
  test("Parse RH dislocation onset factors") {
    val a = new ParsedFactor("6(a)",
      "having physical trauma to the affected joint at the time of the clinical onset of dislocation",
      Set(traumaDef))

    val b = new ParsedFactor("6(b)",
      "having a disease process affecting the normal structural or functional relationship " +
        "between the articulating surfaces of the affected joint at the time of the clinical " +
        "onset of dislocation",
      Set(diseaseProcessDef, neuropathicDef))

    val c = new ParsedFactor("6(c)",
      "having damage to a soft tissue structure as specified, at the time of the " +
        "clinical onset of dislocation",
      Set(softTissueDef))

    val d = new ParsedFactor("6(d)",
      "having laxity of the joint capsule or a stabilising ligament of the affected " +
        "joint, at the time of the clinical onset of dislocation",
      Nil.toSet)

    val e = new ParsedFactor("6(e)",
      "having a fracture, avulsion or bony defect involving the articulating " +
        "surfaces of the affected joint, at the time of the clinical onset of dislocation",
      Nil.toSet)

    val f = new ParsedFactor("6(f)",
      "having a biomechanical abnormality involving the affected joint, at the " +
        "time of the clinical onset of dislocation",
      Set(biomechanicalDef))

    val g = new ParsedFactor("6(g)",
      "for rotational atlantoaxial joint dislocation only,\n(i) having an inflammatory " +
        "or infectious condition involving the ear, nose or throat within the 21 days " +
        "before the clinical onset of dislocation; or\n(ii) having undergone a surgical " +
        "procedure involving the head or neck within the 21 days before the clinical " +
        "onset of dislocation",
      Nil.toSet)

    val h = new ParsedFactor("6(h)",
      "for temporomandibular joint dislocation only,\n(i) undergoing tracheal intubation " +
        "at the time of the clinical onset of dislocation;\n(ii) undergoing intravenous " +
        "sedation at the time of the clinical onset of dislocation; or\n(iii) undergoing " +
        "an activity that involves wide opening of the mouth at the time of the clinical " +
        "onset of dislocation",
      Set(wideOpeningDef))

    val onsetFactors = rhFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 8)
    assert(onsetFactors.contains(a))
    assert(onsetFactors.contains(b))
    assert(onsetFactors.contains(c))
    assert(onsetFactors.contains(d))
    assert(onsetFactors.contains(e))
    assert(onsetFactors.contains(f))
    assert(onsetFactors.contains(g))
    assert(onsetFactors.contains(h))
  }

  // Aggravation factors
  test("Parse RH dislocation aggravation factors") {
    val i = new ParsedFactor("6(i)",
      "inability to obtain appropriate clinical management for dislocation",
      Nil.toSet)

    val aggravationFactors = rhFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 1)
    assert(aggravationFactors.contains(i))
  }

  test("Parse entire BoP dislocation SoP")
  {
    System.out.println(TestUtils.prettyPrint(StoredSop.toJson(bopFixture.result)))
    assert(bopFixture.result != null)
  }

  test("Parse BoP dislocation register ID") {
    assert(bopFixture.result.getRegisterId === "F2010L01041")
  }

  test("Parse BoP dislocation instrument number") {
    val instrumentNumber = bopFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 25)
    assert(instrumentNumber.getYear === 2010)
  }

  test("Parse BoP dislocation citation") {
    assert(bopFixture.result.getCitation === "Statement of Principles concerning " +
      "dislocation No. 25 of 2010")
  }

  test("Parse BoP dislocation condition name") {
    assert(bopFixture.result.getConditionName === "dislocation")
  }

  test("Parse BoP dislocation effective from date") {
    assert(bopFixture.result.getEffectiveFromDate === LocalDate.of(2010, 5, 12))
  }

  test("Parse BoP dislocation standard of proof") {
    assert(bopFixture.result.getStandardOfProof === StandardOfProof.BalanceOfProbabilities)
  }

  // ICD codes
  test("Parse BoP dislocation ICD codes") {
    val icdCodes = bopFixture.result.getICDCodes
    assert(icdCodes.size() === 17)
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M24.3")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M99.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S03.0")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S33.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S33.2")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S33.3")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S43.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S43.2")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S43.3")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S53.0")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S53.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S63.0")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S63.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S63.2")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S93.0")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S93.1")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "S93.3")))
  }

  // Onset factors
  test("Parse BoP dislocation onset factors") {
    val a = new ParsedFactor("6(a)",
      "having physical trauma to the affected joint at the time of the clinical onset of dislocation",
      Set(traumaDef))

    val b = new ParsedFactor("6(b)",
      "having a disease process affecting the normal structural or functional relationship " +
        "between the articulating surfaces of the affected joint at the time of the " +
        "clinical onset of dislocation",
      Set(diseaseProcessDef, neuropathicDef))

    val c = new ParsedFactor("6(c)",
      "having damage to a soft tissue structure as specified, at the time of the " +
        "clinical onset of dislocation",
      Set(softTissueDef))

    val d = new ParsedFactor("6(d)",
      "having laxity of the joint capsule or a stabilising ligament of the affected joint, " +
        "at the time of the clinical onset of dislocation",
      Nil.toSet)

    val e = new ParsedFactor("6(e)",
      "having a fracture, avulsion or bony defect involving the articulating surfaces of " +
        "the affected joint, at the time of the clinical onset of dislocation",
      Nil.toSet)

    val f = new ParsedFactor("6(f)",
      "having a biomechanical abnormality involving the affected joint, at the " +
        "time of the clinical onset of dislocation",
      Set(biomechanicalDef))

    val g = new ParsedFactor("6(g)",
      "for rotational atlantoaxial joint dislocation only,  (i)  having an inflammatory " +
        "or infectious condition involving the ear, nose or throat within the 14 days " +
        "before the clinical onset of dislocation; or (ii) having undergone a surgical " +
        "procedure involving the head or neck within the 14 days before the " +
        "clinical onset of dislocation",
      Nil.toSet)

    val h = new ParsedFactor("6(h)",
      "for temporomandibular joint dislocation only,  (i)  undergoing tracheal intubation " +
        "at the time of the clinical onset of dislocation; (ii) undergoing intravenous sedation " +
        "at the time of the clinical onset of dislocation; or (iii) undergoing an activity " +
        "that involves wide opening of the mouth at the time of the clinical onset of dislocation",
      Set(wideOpeningDef))

    val onsetFactors = bopFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 8)
    assert(onsetFactors.contains(a))
    assert(onsetFactors.contains(b))
    assert(onsetFactors.contains(c))
    assert(onsetFactors.contains(d))
    assert(onsetFactors.contains(e))
    assert(onsetFactors.contains(f))
    assert(onsetFactors.contains(g))
    assert(onsetFactors.contains(h))
  }

  // Aggravation factors
  test("Parse BoP dislocation aggravation factors") {
    val i = new ParsedFactor("6(i)",
      "inability to obtain appropriate clinical management for dislocation",
      Nil.toSet)

    val aggravationFactors = bopFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 1)
    assert(aggravationFactors.contains(i))
  }

}
