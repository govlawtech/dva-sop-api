
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
class LumbarSpondylosisTests extends FunSuite {

  val rhFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2014L00933", "sops_rh/F2014L00933.pdf")
  }

  val bopFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2014L00930", "sops_bop/F2014L00930.pdf")
  }

  val depositionalJointDef = new ParsedDefinedTerm("a depositional joint disease",
    "means gout, calcium pyrophosphate dihydrate\ndeposition disease (also known as pseudogout), " +
      "haemochromatosis, Wilson’s\ndisease or alkaptonuria (also known as ochronosis)")

  val kyphoticDef = new ParsedDefinedTerm("a kyphotic abnormality",
    "means abnormally increased dorsal convexity in\nthe curvature of the lumbar vertebral column")

  val lordoticDef = new ParsedDefinedTerm("a lordotic abnormality",
    "means abnormally increased dorsal concavity in the\ncurvature of the lumbar vertebral column")

  val rhSpinalDef = new ParsedDefinedTerm("a specified spinal condition",
    "means:\n(a) a deformity of a joint of a vertebra;\n(b) a deformity of a vertebra;\n" +
      "(c) a kyphotic abnormality;\n(d) a lordotic abnormality;\n(e) necrosis of bone;\n" +
      "(f) retrospondylolisthesis;\n(g) scoliosis; or\n(h) spondylolisthesis")

  val bopSpinalDef = new ParsedDefinedTerm("a specified spinal condition",
    "means:\n(a) a deformity of a joint of a vertebra;\n(b) a deformity of a vertebra;\n" +
      "(c) necrosis of bone;\n(d) retrospondylolisthesis;\n(e) scoliosis; or\n(f) spondylolisthesis")

  val infectionDef = new ParsedDefinedTerm("an infection of the affected joint as specified",
    "means bacterial or fungal\ninfection of the affected joint in the lumbar spine " +
      "resulting in inflammation\nwithin that joint")

  val intraArticularDef = new ParsedDefinedTerm("an intra-articular fracture",
    "means a fracture involving any articular surface\nof the affected joint")

  val obeseDef = new ParsedDefinedTerm("being obese",
    "means an increase in body weight by way of fat accumulation\nwhich results in a Body " +
      "Mass Index (BMI) of thirty or greater.\nThe BMI = W/H\u00B2 and where:\nW is the person's " +
      "weight in kilograms; and\nH is the person's height in metres")

  val forwardFlexionDef = new ParsedDefinedTerm("extreme forward flexion of the lumbar spine",
    "means being in a posture\ninvolving greater than 90 degrees of trunk flexion")

  val gForceDef = new ParsedDefinedTerm("G force",
    "means the ratio of the applied acceleration of the aircraft to the\nacceleration " +
      "due to gravity, for example, 4G = 4 x 9.81m/s2")

  val inflammatoryJointDef = new ParsedDefinedTerm("inflammatory joint disease",
    "means rheumatoid arthritis, reactive arthritis,\npsoriatic arthropathy, ankylosing " +
      "spondylitis, or arthritis associated with\nCrohn's disease or ulcerative colitis")

  val legLengthDef = new ParsedDefinedTerm("leg length inequality",
    "means a clinically significant disparity of at least\nthree percent or three " +
      "centimetres in leg length, whichever is the lesser, where\nthe inequality " +
      "remains uncorrected and involves the limb in daily use")

  val liftingLoadsDef = new ParsedDefinedTerm("lifting loads",
    "means manually raising an object")

  val lumbarTraumaDef = new ParsedDefinedTerm("trauma to the lumbar spine",
    "means a discrete event involving the\napplication of significant physical force, " +
      "including G force, to the lumbar spine\nthat causes the development within " +
      "twenty-four hours of the injury being\nsustained, of symptoms and signs of " +
      "pain and tenderness and either altered\nmobility or range of movement of the " +
      "lumbar spine. In the case of sustained\nunconsciousness or the masking of pain " +
      "by analgesic medication, these\nsymptoms and signs must appear on return to " +
      "consciousness or the withdrawal\nof the analgesic medication. These symptoms " +
      "and signs must last for a period\nof at least seven days following their onset; " +
      "save for where medical\nintervention has occurred and that medical intervention " +
      "involves either:\n(a) immobilisation of the lumbar spine by splinting, or similar " +
      "external\nagent;\n(b) injection of corticosteroids or local anaesthetics into the " +
      "lumbar spine; or\n(c) surgery to the lumbar spine")

  test("Parse entire RH LS SoP") {
    System.out.print(TestUtils.prettyPrint(StoredSop.toJson(rhFixture.result)))
    assert(rhFixture.result != null)
  }

  test("Parse RH lumbar spondylosis register ID") {
    assert(rhFixture.result.getRegisterId === "F2014L00933")
  }

  test("Parse RH lumbar spondylosis instrument number") {
    val instrumentNumber = rhFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 62)
    assert(instrumentNumber.getYear === 2014)
  }

  test("Parse RH lumbar spondylosis citation") {
    assert(rhFixture.result.getCitation === "Statement of Principles concerning " +
      "lumbar spondylosis No. 62 of 2014")
  }

  test("Parse RH lumbar spondylosis condition name") {
    assert(rhFixture.result.getConditionName === "lumbar spondylosis")
  }

  test("Parse RH lumbar spondylosis effective from date") {
    assert(rhFixture.result.getEffectiveFromDate === LocalDate.of(2014, 7, 2))
  }

  test("Parse RH lumbar spondylosis standard of proof") {
    assert(rhFixture.result.getStandardOfProof === StandardOfProof.ReasonableHypothesis)
  }

  // ICD codes
  test("Parse RH lumbar spondylosis ICD codes") {
    val icdCodes = rhFixture.result.getICDCodes
    assert(icdCodes.size() === 9)
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.16")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.17")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.26")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.27")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.86")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.87")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.96")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.97")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M51.3")))
  }

  // Onset factors
  // Needs lots of work to update this test
  test("Parse RH lumbar spondylosis onset factors") {
    val a = new ParsedFactor("6(a)",
      "being a prisoner of war before the clinical onset of lumbar spondylosis",
      Nil.toSet)

    val b = new ParsedFactor("6(b)",
      "having inflammatory joint disease in the lumbar spine before the clinical " +
        "onset of lumbar spondylosis",
      Set(inflammatoryJointDef))

    val c = new ParsedFactor("6(c)",
      "having an infection of the affected joint as specified at least one " +
        "year before the clinical onset of lumbar spondylosis",
      Set(infectionDef))

    val d = new ParsedFactor("6(d)",
      "having an intra-articular fracture of the lumbar spine at least one year " +
        "before the clinical onset of lumbar spondylosis",
      Set(intraArticularDef))

    val e = new ParsedFactor("6(e)",
      "having a specified spinal condition affecting the lumbar spine for at least " +
        "the one year before the clinical onset of lumbar spondylosis",
      Set(kyphoticDef, lordoticDef, rhSpinalDef))

    val f = new ParsedFactor("6(f)",
      "having leg length inequality for at least the two years before the clinical " +
        "onset of lumbar spondylosis",
      Set(legLengthDef))

    val g = new ParsedFactor("6(g)",
      "having a depositional joint disease in the lumbar spine before the clinical " +
        "onset of lumbar spondylosis",
      Set(depositionalJointDef))

    val h = new ParsedFactor("6(h)",
      "having trauma to the lumbar spine at least one year before the clinical onset " +
        "of lumbar spondylosis",
      Set(gForceDef, lumbarTraumaDef))

    val i = new ParsedFactor("6(i)",
      "having a lumbar intervertebral disc prolapse before the clinical onset of " +
        "lumbar spondylosis at the level of the intervertebral disc prolapse",
      Nil.toSet)

    val j = new ParsedFactor("6(j)",
      "lifting loads of at least 25 kilograms while bearing weight through the lumbar " +
        "spine to a cumulative total of at least 120 000 kilograms within any ten year " +
        "period before the clinical onset of lumbar spondylosis",
      Set(liftingLoadsDef))

    val k = new ParsedFactor("6(k)",
      "carrying loads of at least 25 kilograms while bearing weight through the lumbar " +
        "spine to a cumulative total of at least 3 800 hours within any ten year period " +
        "before the clinical onset of lumbar spondylosis",
      Nil.toSet)

    val l = new ParsedFactor("6(l)",
      "being obese for at least ten years before the clinical onset of lumbar spondylosis",
      Set(obeseDef))

    val m = new ParsedFactor("6(m)",
      "flying in a powered aircraft as operational aircrew, for a cumulative total of at " +
        "least 1 000 hours within the 25 years before the clinical onset of lumbar spondylosis",
      Nil.toSet)

    val n = new ParsedFactor("6(n)",
      "extreme forward flexion of the lumbar spine for a cumulative total of at least " +
        "1 500 hours before the clinical onset of lumbar spondylosis",
      Set(forwardFlexionDef))

    val o = new ParsedFactor("6(o)",
      "having acromegaly involving the lumbar spine before the clinical onset of lumbar spondylosis",
      Nil.toSet)

    val p = new ParsedFactor("6(p)",
      "having Paget's disease of bone involving the lumbar spine before the clinical onset of lumbar spondylosis",
      Nil.toSet)

    val onsetFactors = rhFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 16)
    assert(onsetFactors.contains(a))
    assert(onsetFactors.contains(b))
    assert(onsetFactors.contains(c))
    assert(onsetFactors.contains(d))
    assert(onsetFactors.contains(e))
    assert(onsetFactors.contains(f))
    assert(onsetFactors.contains(g))
    assert(onsetFactors.contains(h))
    assert(onsetFactors.contains(i))
    assert(onsetFactors.contains(j))
    assert(onsetFactors.contains(k))
     assert(onsetFactors.contains(l))
    assert(onsetFactors.contains(m))
    assert(onsetFactors.contains(n))
    assert(onsetFactors.contains(o))
    assert(onsetFactors.contains(p))
  }

  // Aggravation factors
  ignore("Parse RH lumbar spondylosis aggravation factors") {
    val q = new ParsedFactor("6(q)",
      "having inflammatory joint disease in the lumbar spine before the clinical " +
        "worsening of lumbar spondylosis",
      Set(inflammatoryJointDef))

    val r = new ParsedFactor("6(r)",
      "having an infection of the affected joint as specified at least one " +
        "year before the clinical worsening of lumbar spondylosis",
      Set(infectionDef))

    val s = new ParsedFactor("6(s)",
      "having an intra-articular fracture of the lumbar spine at least one " +
        "year before the clinical worsening of lumbar spondylosis",
      Set(intraArticularDef))

    val t = new ParsedFactor("6(t)",
      "having a specified spinal condition affecting the lumbar spine for at " +
        "least the one year before the clinical worsening of lumbar spondylosis",
      Set(kyphoticDef, lordoticDef, rhSpinalDef))

    val u = new ParsedFactor("6(u)",
      "having leg length inequality for at least the two years before the " +
        "clinical worsening of lumbar spondylosis",
      Set(legLengthDef))

    val v = new ParsedFactor("6(v)",
      "having a depositional joint disease in the lumbar spine before the " +
        "clinical worsening of lumbar spondylosis",
      Set(depositionalJointDef))

    val w = new ParsedFactor("6(w)",
      "having trauma to the lumbar spine at least one year before the clinical " +
        "worsening of lumbar spondylosis",
      Set(gForceDef, lumbarTraumaDef))

    val x = new ParsedFactor("6(x)",
      "having a lumbar intervertebral disc prolapse before the clinical worsening " +
        "of lumbar spondylosis at the level of the intervertebral disc prolapse",
      Nil.toSet)

    val y = new ParsedFactor("6(y)",
      "lifting loads of at least 25 kilograms while bearing weight through the " +
        "lumbar spine to a cumulative total of at least 120 000 kilograms within " +
        "any ten year period before the clinical worsening of lumbar spondylosis",
      Set(liftingLoadsDef))

    val z = new ParsedFactor("6(z)",
      "carrying loads of at least 25 kilograms while bearing weight through the " +
        "lumbar spine to a cumulative total of at least 3 800 hours within any ten " +
        "year period before the clinical worsening of lumbar spondylosis",
      Nil.toSet)

    val aa = new ParsedFactor("6(aa)",
      "being obese for at least ten years before the clinical worsening of lumbar spondylosis",
      Set(obeseDef))

    val bb = new ParsedFactor("6(bb)",
      "flying in a powered aircraft as operational aircrew, for a cumulative total of at " +
        "least 1 000 hours within the 25 years before the clinical worsening of lumbar spondylosis",
      Nil.toSet)

    val cc = new ParsedFactor("6(cc)",
      "extreme forward flexion of the lumbar spine for a cumulative total of at least " +
        "1 500 hours before the clinical worsening of lumbar spondylosis",
      Set(forwardFlexionDef))

    val dd = new ParsedFactor("6(dd)",
      "having acromegaly involving the lumbar spine before the clinical worsening of " +
        "lumbar spondylosis",
      Nil.toSet)

    val ee = new ParsedFactor("6(ee)",
      "having Paget's disease of bone involving the lumbar spine before the clinical " +
        "worsening of lumbar spondylosis",
      Nil.toSet)

    val ff = new ParsedFactor("6(ff)",
      "inability to obtain appropriate clinical management for lumbar spondylosis",
      Nil.toSet)

    val aggravationFactors = rhFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 16)
    assert(aggravationFactors.contains(q))
    assert(aggravationFactors.contains(r))
    assert(aggravationFactors.contains(s))
    assert(aggravationFactors.contains(t))
    assert(aggravationFactors.contains(u))
    assert(aggravationFactors.contains(v))
    assert(aggravationFactors.contains(w))
    assert(aggravationFactors.contains(x))
    assert(aggravationFactors.contains(y))
    assert(aggravationFactors.contains(z))
    assert(aggravationFactors.contains(aa))
    assert(aggravationFactors.contains(bb))
    assert(aggravationFactors.contains(cc))
    assert(aggravationFactors.contains(dd))
    assert(aggravationFactors.contains(ee))
    assert(aggravationFactors.contains(ff))
  }

  test("Parse entire BoP LS SoP")
  {
    System.out.println(TestUtils.prettyPrint(StoredSop.toJson(bopFixture.result)))
    assert(bopFixture.result != null)
  }

  test("Parse BoP lumbar spondylosis register ID") {
    assert(bopFixture.result.getRegisterId === "F2014L00930")
  }

  test("Parse BoP lumbar spondylosis instrument number") {
    val instrumentNumber = bopFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 63)
    assert(instrumentNumber.getYear === 2014)
  }

  test("Parse BoP lumbar spondylosis citation") {
    assert(bopFixture.result.getCitation === "Statement of Principles concerning " +
      "lumbar spondylosis No. 63 of 2014")
  }

  test("Parse BoP lumbar spondylosis condition name") {
    assert(bopFixture.result.getConditionName === "lumbar spondylosis")
  }

  test("Parse BoP lumbar spondylosis effective from date") {
    assert(bopFixture.result.getEffectiveFromDate === LocalDate.of(2014, 7, 2))
  }

  test("Parse BoP lumbar spondylosis standard of proof") {
    assert(bopFixture.result.getStandardOfProof === StandardOfProof.BalanceOfProbabilities)
  }

  // ICD codes
  test("Parse BoP lumbar spondylosis ICD codes") {
    val icdCodes = bopFixture.result.getICDCodes
    assert(icdCodes.size() === 9)
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.16")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.17")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.26")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.27")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.86")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.87")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.96")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M47.97")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "M51.3")))
  }

  // Onset factors
  ignore("Parse BoP lumbar spondylosis onset factors") {
    val a = new ParsedFactor("6(a)",
      "having inflammatory joint disease in the lumbar spine before the clinical " +
        "onset of lumbar spondylosis",
      Set(inflammatoryJointDef))

    val b = new ParsedFactor("6(b)",
      "having an infection of the affected joint as specified at least one year " +
        "before the clinical onset of lumbar spondylosis",
      Set(infectionDef))

    val c = new ParsedFactor("6(c)",
      "having an intra-articular fracture of the lumbar spine at least one year " +
        "before the clinical onset of lumbar spondylosis",
      Set(intraArticularDef))

    val d = new ParsedFactor("6(d)",
      "having a specified spinal condition affecting the lumbar spine for at " +
        "least the one year before the clinical onset of lumbar spondylosis",
      Set(bopSpinalDef))

    val e = new ParsedFactor("6(e)",
      "having leg length inequality for at least the five years before the clinical " +
        "onset of lumbar spondylosis",
      Set(legLengthDef))

    val f = new ParsedFactor("6(f)",
      "having a depositional joint disease in the lumbar spine before the clinical " +
        "onset of lumbar spondylosis",
      Set(depositionalJointDef))

    val g = new ParsedFactor("6(g)",
      "having trauma to the lumbar spine at least one year before the clinical onset " +
        "of lumbar spondylosis, and where the trauma to the lumbar spine occurred " +
        "within the 25 years before the clinical onset of lumbar spondylosis",
      Set(gForceDef, lumbarTraumaDef))

    val h = new ParsedFactor("6(h)",
      "having a lumbar intervertebral disc prolapse before the clinical onset of lumbar " +
        "spondylosis at the level of the intervertebral disc prolapse",
      Nil.toSet)

    val i = new ParsedFactor("6(i)",
      "lifting loads of at least 35 kilograms while bearing weight through the lumbar " +
        "spine to a cumulative total of at least 168 000 kilograms within any ten year " +
        "period before the clinical onset of lumbar spondylosis, and where the clinical " +
        "onset of lumbar spondylosis occurs within the 25 years following that period",
      Set(liftingLoadsDef))

    val j = new ParsedFactor("6(j)",
      "carrying loads of at least 35 kilograms while bearing weight through the lumbar " +
        "spine to a cumulative total of at least 3 800 hours within any ten year period " +
        "before the clinical onset of lumbar spondylosis, and where the clinical onset " +
        "of lumbar spondylosis occurs within the 25 years following that period",
      Nil.toSet)

    val k = new ParsedFactor("6(k)",
      "being obese for at least ten years within the 25 years before the clinical onset " +
        "of lumbar spondylosis",
      Set(obeseDef))

    val l = new ParsedFactor("6(l)",
      "flying in a powered aircraft as operational aircrew, for a cumulative total of at least 2 000 " +
        "hours within the 25 years before the clinical onset of lumbar spondylosis",
      Nil.toSet)

    val m = new ParsedFactor("6(m)",
      "extreme forward flexion of the lumbar spine for a cumulative total of at least 1 500 hours " +
        "before the clinical onset of lumbar spondylosis",
      Set(forwardFlexionDef))

    val n = new ParsedFactor("6(n)",
      "having acromegaly involving the lumbar spine before the clinical onset of lumbar spondylosis",
      Nil.toSet)

    val o = new ParsedFactor("6(o)",
      "having Paget's disease of bone involving the lumbar spine before the clinical onset of " +
        "lumbar spondylosis",
      Nil.toSet)

    val onsetFactors = bopFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 15)
    assert(onsetFactors.contains(a))
    assert(onsetFactors.contains(b))
    assert(onsetFactors.contains(c))
    assert(onsetFactors.contains(d))
    assert(onsetFactors.contains(e))
    assert(onsetFactors.contains(f))
    assert(onsetFactors.contains(g))
    assert(onsetFactors.contains(h))
    assert(onsetFactors.contains(i))
    assert(onsetFactors.contains(j))
    assert(onsetFactors.contains(k))
    assert(onsetFactors.contains(l))
    assert(onsetFactors.contains(m))
    assert(onsetFactors.contains(n))
    assert(onsetFactors.contains(o))
  }

  // Aggravation factors
  ignore("Parse BoP lumbar spondylosis aggravation factors") {
    val p = new ParsedFactor("6(p)",
      "having inflammatory joint disease in the lumbar spine before the clinical " +
        "worsening of lumbar spondylosis",
      Set(inflammatoryJointDef))

    val q = new ParsedFactor("6(q)",
      "having an infection of the affected joint as specified at least one year " +
        "before the clinical worsening of lumbar spondylosis",
      Set(infectionDef))

    val r = new ParsedFactor("6(r)",
      "having an intra-articular fracture of the lumbar spine at least one year before " +
        "the clinical worsening of lumbar spondylosis",
      Set(intraArticularDef))

    val s = new ParsedFactor("6(s)",
      "having a specified spinal condition affecting the lumbar spine for at least " +
        "the one year before the clinical worsening of lumbar spondylosis",
      Set(bopSpinalDef))

    val t = new ParsedFactor("6(t)",
      "having leg length inequality for at least the five years before the clinical " +
        "worsening of lumbar spondylosis",
      Set(legLengthDef))

    val u = new ParsedFactor("6(u)",
      "having a depositional joint disease in the lumbar spine before the clinical " +
        "worsening of lumbar spondylosis",
      Set(depositionalJointDef))

    val v = new ParsedFactor("6(v)",
      "having trauma to the lumbar spine at least one year before the clinical " +
        "worsening of lumbar spondylosis, and where the trauma to the lumbar spine " +
        "occurred within the 25 years before the clinical worsening of lumbar spondylosis",
      Set(gForceDef, lumbarTraumaDef))

    val w = new ParsedFactor("6(w)",
      "having a lumbar intervertebral disc prolapse before the clinical worsening of lumbar " +
        "spondylosis at the level of the intervertebral disc prolapse",
      Nil.toSet)

    val x = new ParsedFactor("6(x)",
      "lifting loads of at least 35 kilograms while bearing weight through the lumbar " +
        "spine to a cumulative total of at least 168 000 kilograms within any ten year " +
        "period before the clinical worsening of lumbar spondylosis, and where the clinical " +
        "worsening of lumbar spondylosis occurs within the 25 years following that period",
      Set(liftingLoadsDef))

    val y = new ParsedFactor("6(y)",
      "carrying loads of at least 35 kilograms while bearing weight through the lumbar " +
        "spine to a cumulative total of at least 3 800 hours within any ten year period " +
        "before the clinical worsening of lumbar spondylosis, and where the clinical " +
        "worsening of lumbar spondylosis occurs within the 25 years following that period",
      Nil.toSet)

    val z = new ParsedFactor("6(z)",
      "being obese for at least ten years within the 25 years before the clinical " +
        "worsening of lumbar spondylosis",
      Set(obeseDef))

    val aa = new ParsedFactor("6(aa)",
      "flying in a powered aircraft as operational aircrew, for a cumulative total of at least " +
        "2 000 hours within the 25 years before the clinical worsening of lumbar spondylosis",
      Nil.toSet)

    val bb = new ParsedFactor("6(bb)",
      "extreme forward flexion of the lumbar spine for a cumulative total of at least 1 500 " +
        "hours before the clinical worsening of lumbar spondylosis",
      Set(forwardFlexionDef))

    val cc = new ParsedFactor("6(cc)",
      "having acromegaly involving the lumbar spine before the clinical worsening of " +
        "lumbar spondylosis",
      Nil.toSet)

    val dd = new ParsedFactor("6(dd)",
      "having Paget's disease of bone involving the lumbar spine before the clinical " +
        "worsening of lumbar spondylosis",
      Nil.toSet)

    val ee = new ParsedFactor("6(ee)",
      "inability to obtain appropriate clinical management for lumbar spondylosis",
      Nil.toSet)

    val aggravationFactors = bopFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 16)
    assert(aggravationFactors.contains(p))
    assert(aggravationFactors.contains(q))
    assert(aggravationFactors.contains(r))
    assert(aggravationFactors.contains(s))
    assert(aggravationFactors.contains(t))
    assert(aggravationFactors.contains(u))
    assert(aggravationFactors.contains(v))
    assert(aggravationFactors.contains(w))
    assert(aggravationFactors.contains(x))
    assert(aggravationFactors.contains(y))
    assert(aggravationFactors.contains(z))
    assert(aggravationFactors.contains(aa))
    assert(aggravationFactors.contains(bb))
    assert(aggravationFactors.contains(cc))
    assert(aggravationFactors.contains(dd))
    assert(aggravationFactors.contains(ee))
  }

}
