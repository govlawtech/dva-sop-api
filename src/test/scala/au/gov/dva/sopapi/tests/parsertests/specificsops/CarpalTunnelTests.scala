
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
class CarpalTunnelTests extends FunSuite {

  val rhFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2013L00022", "sops_rh/F2013L00022.pdf")
  }

  val bopFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2013L00023", "sops_bop/F2013L00023.pdf")
  }

  val acromegalyDef = new ParsedDefinedTerm("acromegaly",
    "means a chronic disease of adults due to hypersecretion of the\npituitary growth " +
      "hormone and characterised by enlargement of many parts of\nthe skeleton especially " +
      "the distal portions, the nose, ears, jaws, fingers and\ntoes")

  val amyloidosisDef = new ParsedDefinedTerm("amyloidosis",
    "means the accumulation of insoluble fibrillar proteins\nin organs or tissues of the " +
      "body such that vital function is compromised")

  val aromataseDef = new ParsedDefinedTerm("an aromatase inhibitor",
    "means:\n(a) anastrozole;\n(b) exemestane; or\n(c) letrozole")

  val obeseDef = new ParsedDefinedTerm("being obese",
    "means an increase in body weight by way of fat accumulation\nwhich results in a " +
      "Body Mass Index (BMI) of thirty or greater.\nThe BMI = W/H^2 and where:\nW is the " +
      "person’s weight in kilograms; and\nH is the person’s height in metres")

  val forcefulDef = new ParsedDefinedTerm("forceful activities",
    "mean tasks requiring the generation of force by the hand:\n(a) equivalent to " +
      "lifting or carrying loads of more than three kilograms; or\n(b) involving " +
      "lifting or carrying an object in the hand greater than one\nkilogram in excess " +
      "of ten times per hour")

  val arthritisDef = new ParsedDefinedTerm("inflammatory arthritis",
    "means one of the following diseases:\n(a) ankylosing spondylitis;\n(b) arthropathy " +
      "associated with inflammatory bowel disease;\n(c) chondrocalcinosis (also known as " +
      "pseudogout);\n(d) dermatomyositis;\n(e) eosinophilia myalgia syndrome;\n(f) " +
      "eosinophilic fasciitis;\n(g) multifocal fibrosclerosis;\n(h) other inflammatory " +
      "arthritis requiring treatment with a disease\nmodifying agent or a biological agent;\n" +
      "(i) polymyositis;\n(j) psoriatic arthropathy;\n(k) reactive arthritis;\n(l) rheumatoid " +
      "arthritis;\n(m) sarcoidosis;\n(n) sicca syndrome (which includes keratoconjunctivitis " +
      "sicca and Sjogren's\ndisease);\n(o) systemic fibrosclerosing syndrome;\n(p) systemic " +
      "lupus erythematosus;\n(q) systemic sclerosis (which includes circumscribed scleroderma " +
      "and\nCREST syndrome); or\n(r) unspecified diffuse connective tissue disease")

  val oedemaDef = new ParsedDefinedTerm("oedema",
    "means the presence of abnormally large amounts of fluid in the\nintercellular tissue " +
      "spaces of the body and demonstrated by accumulation of\nexcessive fluid in the " +
      "subcutaneous tissues")

  val repetitiveActivitiesDef = new ParsedDefinedTerm("repetitive activities",
    "mean:\n(a) bending or twisting of the hand or wrist; or\n(b) carrying out the same " +
      "or similar movements in the hand or wrist,\nat least 50 times per hour")

  val lesionDef = new ParsedDefinedTerm("space-occupying lesion in the affected carpal tunnel",
    "means one of the\nfollowing entities occupying space within the carpal tunnel:\n" +
      "(a) aneurysm;\n(b) calcification;\n(c) cyst;\n(d) ganglion; or\n(e) neoplasm")

  test("Parse entire RH LS SoP") {
    System.out.print(TestUtils.prettyPrint(StoredSop.toJson(rhFixture.result)))
    assert(rhFixture.result != null)
  }

  test("Parse RH carpal tunnel syndrome register ID") {
    assert(rhFixture.result.getRegisterId === "F2013L00022")
  }

  test("Parse RH carpal tunnel syndrome instrument number") {
    val instrumentNumber = rhFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 7)
    assert(instrumentNumber.getYear === 2013)
  }

  test("Parse RH carpal tunnel syndrome citation") {
    assert(rhFixture.result.getCitation === "Statement of Principles concerning " +
      "carpal tunnel syndrome No. 7 of 2013")
  }

  test("Parse RH carpal tunnel syndrome condition name") {
    assert(rhFixture.result.getConditionName === "carpal tunnel syndrome")
  }

  test("Parse RH carpal tunnel syndrome effective from date") {
    assert(rhFixture.result.getEffectiveFromDate === LocalDate.of(2013, 1, 9))
  }

  test("Parse RH carpal tunnel syndrome standard of proof") {
    assert(rhFixture.result.getStandardOfProof === StandardOfProof.ReasonableHypothesis)
  }

  // ICD codes
  test("Parse RH carpal tunnel syndrome ICD codes") {
    val icdCodes = rhFixture.result.getICDCodes
    assert(icdCodes.size() === 1)
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "G56.0")))
  }

  // Onset factors
  test("Parse RH carpal tunnel syndrome onset factors") {
    val a = new ParsedFactor("6(a)",
      "performing any combination of repetitive activities or forceful activities with " +
        "the affected hand for at least 130 hours within a period of 120 consecutive days " +
        "before the clinical onset of carpal tunnel syndrome, and where the repetitive or " +
        "forceful activities have not ceased more than 30 days before the clinical onset of " +
        "carpal tunnel syndrome",
      Set(forcefulDef, repetitiveActivitiesDef))

    val b = new ParsedFactor("6(b)",
      "daily self-propulsion of a manual wheelchair for at least a cumulative period of 130 " +
        "hours within a period of 120 consecutive days before the clinical onset of carpal " +
        "tunnel syndrome, and where the self-propulsion of a manual wheelchair has not ceased " +
        "more than 30 days before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val c = new ParsedFactor("6(c)",
      "performing activities where the affected hand or forearm is directly vibrated for at " +
        "least 130 hours within a period of 120 consecutive days before the clinical onset of " +
        "carpal tunnel syndrome, and where those activities have not ceased more than 30 days " +
        "before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val d = new ParsedFactor("6(d)",
      "(d) having an injury to the affected wrist or hand which does not involve a\nfracture or " +
        "a dislocation but which:\n(i) alters the normal contour of the carpal tunnel; or\n" +
        "(ii) damages the flexor tendons within the carpal tunnel,\nwithin the one year before " +
        "the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val e = new ParsedFactor("6(e)",
      "(e) having a fracture or dislocation to the distal radius, the distal ulna, a carpal bone " +
        "or a metacarpal bone of the affected side which:\n(i) alters the normal contour of " +
        "the carpal tunnel; or\n(ii) damages the flexor tendons within the carpal tunnel,\n" +
        "before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val f = new ParsedFactor("6(f)",
      "(f) having surgery to the affected wrist or hand which:\n(i) alters the normal contour of " +
        "the carpal tunnel; or\n(ii) damages the flexor tendons within the carpal tunnel,\n" +
        "within the one year before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val g = new ParsedFactor("6(g)",
      "being obese at the time of the clinical onset of carpal tunnel syndrome",
      Set(obeseDef))

    val h = new ParsedFactor("6(h)",
      "undergoing haemodialysis or peritoneal dialysis for at least the one year before " +
        "the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val i = new ParsedFactor("6(i)",
      "having hypothyroidism at the time of the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val j = new ParsedFactor("6(j)",
      "having acromegaly before the clinical onset of carpal tunnel syndrome",
      Set(acromegalyDef))

    val k = new ParsedFactor("6(k)",
      "having amyloidosis at the time of the clinical onset of carpal tunnel syndrome",
      Set(amyloidosisDef))

    val l = new ParsedFactor("6(l)",
      "having gout in the affected wrist or hand at the time of the clinical onset " +
        "of carpal tunnel syndrome",
      Nil.toSet)

    val m = new ParsedFactor("6(m)",
      "having a space-occupying lesion in the affected carpal tunnel at the " +
        "time of the clinical onset of carpal tunnel syndrome",
      Set(lesionDef))

    val n = new ParsedFactor("6(n)",
      "having oedema involving the affected carpal tunnel at the time of the " +
        "clinical onset of carpal tunnel syndrome",
      Set(oedemaDef))

    val o = new ParsedFactor("6(o)",
      "having an external burn involving the affected wrist or palm requiring " +
        "hospitalisation within the five years before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val p = new ParsedFactor("6(p)",
      "having haemorrhage involving the affected carpal tunnel at the time of the " +
        "clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val q = new ParsedFactor("6(q)",
      "having infection involving the affected carpal tunnel at the time of the " +
        "clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val r = new ParsedFactor("6(r)",
      "having inflammatory arthritis of the affected wrist or hand at the time of the " +
        "clinical onset of carpal tunnel syndrome",
      Set(arthritisDef))

    val s = new ParsedFactor("6(s)",
      "having osteoarthritis of the affected wrist, carpus or trapeziometacarpal joint " +
        "of the thumb at the time of the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val t = new ParsedFactor("6(t)",
      "having diabetes mellitus at the time of the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val u = new ParsedFactor("6(u)",
      "being treated with an aromatase inhibitor within the six months before the " +
        "clinical onset of carpal tunnel syndrome",
      Set(aromataseDef))

    val v = new ParsedFactor("6(v)",
      "being pregnant within the three months before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val onsetFactors = rhFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 22)
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
    assert(onsetFactors.contains(q))
    assert(onsetFactors.contains(r))
    assert(onsetFactors.contains(s))
    assert(onsetFactors.contains(t))
    assert(onsetFactors.contains(u))
    assert(onsetFactors.contains(v))
  }

  // Aggravation factors
  test("Parse RH carpal tunnel syndrome aggravation factors") {
    val w = new ParsedFactor("6(w)",
      "performing any combination of repetitive activities or forceful activities with " +
        "the affected hand for at least 130 hours within a period of 120 consecutive days " +
        "before the clinical worsening of carpal tunnel syndrome, and where the repetitive " +
        "or forceful activities have not ceased more than 30 days before the clinical " +
        "worsening of carpal tunnel syndrome",
      Set(forcefulDef, repetitiveActivitiesDef))

    val x = new ParsedFactor("6(x)",
      "daily self-propulsion of a manual wheelchair for at least a cumulative period of 130 " +
        "hours within a period of 120 consecutive days before the clinical worsening of carpal " +
        "tunnel syndrome, and where the selfpropulsion of a manual wheelchair has not ceased " +
        "more than 30 days before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val y = new ParsedFactor("6(y)",
      "performing activities where the affected hand or forearm is directly vibrated for at " +
        "least 130 hours within a period of 120 consecutive days before the clinical worsening " +
        "of carpal tunnel syndrome, and where those activities have not ceased more than 30 " +
        "days before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val z = new ParsedFactor("6(z)",
      "having an injury to the affected wrist or hand which does not involve a fracture or a " +
        "dislocation but which:\n(i) alters the normal contour of the carpal tunnel; or\n" +
        "(ii) damages the flexor tendons within the carpal tunnel,\nwithin the one year " +
        "before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val aa = new ParsedFactor("6(aa)",
      "having a fracture or dislocation to the distal radius, the distal ulna, a carpal bone " +
        "or a metacarpal bone of the affected side which:\n(i) alters the normal contour of the " +
        "carpal tunnel; or\n(ii) damages the flexor tendons within the carpal tunnel,\n" +
        "before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val bb = new ParsedFactor("6(bb)",
      "having surgery to the affected wrist or hand which:\n(i) alters the normal contour of " +
        "the carpal tunnel; or\n(ii) damages the flexor tendons within the carpal tunnel,\n" +
        "within the one year before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val cc = new ParsedFactor("6(cc)",
      "being obese at the time of the clinical worsening of carpal tunnel syndrome",
      Set(obeseDef))

    val dd = new ParsedFactor("6(dd)",
      "undergoing haemodialysis or peritoneal dialysis for at least the one year before " +
        "the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val ee = new ParsedFactor("6(ee)",
      "having hypothyroidism at the time of the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val ff = new ParsedFactor("6(ff)",
      "having acromegaly before the clinical worsening of carpal tunnel syndrome",
      Set(acromegalyDef))

    val gg = new ParsedFactor("6(gg)",
      "having amyloidosis at the time of the clinical worsening of carpal tunnel syndrome",
      Set(amyloidosisDef))

    val hh = new ParsedFactor("6(hh)",
      "having gout in the affected wrist or hand at the time of the clinical " +
        "worsening of carpal tunnel syndrome",
      Nil.toSet)

    val ii = new ParsedFactor("6(ii)",
      "having a space-occupying lesion in the affected carpal tunnel at the time " +
        "of the clinical worsening of carpal tunnel syndrome",
      Set(lesionDef))

    val jj = new ParsedFactor("6(jj)",
      "having oedema involving the affected carpal tunnel at the time " +
        "of the clinical worsening of carpal tunnel syndrome",
      Set(oedemaDef))

    val kk = new ParsedFactor("6(kk)",
      "having an external burn involving the affected wrist or palm requiring " +
        "hospitalisation within the five years before the clinical worsening " +
        "of carpal tunnel syndrome",
      Nil.toSet)

    val ll = new ParsedFactor("6(ll)",
      "having haemorrhage involving the affected carpal tunnel at the time of the " +
        "clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val mm = new ParsedFactor("6(mm)",
      "having infection involving the affected carpal tunnel at the time of the " +
        "clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val nn = new ParsedFactor("6(nn)",
      "having inflammatory arthritis of the affected wrist or hand at the time " +
        "of the clinical worsening of carpal tunnel syndrome",
      Set(arthritisDef))

    val oo = new ParsedFactor("6(oo)",
      "having osteoarthritis of the affected wrist, carpus or trapeziometacarpal " +
        "joint of the thumb at the time of the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val pp = new ParsedFactor("6(pp)",
      "having diabetes mellitus at the time of the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val qq = new ParsedFactor("6(qq)",
      "being treated with an aromatase inhibitor within the six months before the " +
        "clinical worsening of carpal tunnel syndrome",
      Set(aromataseDef))

    val rr = new ParsedFactor("6(rr)",
      "being pregnant within the three months before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val ss = new ParsedFactor("6(ss)",
      "inability to obtain appropriate clinical management for carpal tunnel syndrome",
      Nil.toSet)

    val aggravationFactors = rhFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 23)
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
    assert(aggravationFactors.contains(gg))
    assert(aggravationFactors.contains(hh))
    assert(aggravationFactors.contains(ii))
    assert(aggravationFactors.contains(jj))
    assert(aggravationFactors.contains(kk))
    assert(aggravationFactors.contains(ll))
    assert(aggravationFactors.contains(mm))
    assert(aggravationFactors.contains(nn))
    assert(aggravationFactors.contains(oo))
    assert(aggravationFactors.contains(pp))
    assert(aggravationFactors.contains(qq))
    assert(aggravationFactors.contains(rr))
    assert(aggravationFactors.contains(ss))
  }

  test("Parse entire BoP LS SoP")
  {
    System.out.println(TestUtils.prettyPrint(StoredSop.toJson(bopFixture.result)))
    assert(bopFixture.result != null)
  }

  test("Parse BoP carpal tunnel syndrome register ID") {
    assert(bopFixture.result.getRegisterId === "F2013L00023")
  }

  test("Parse BoP carpal tunnel syndrome instrument number") {
    val instrumentNumber = bopFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 8)
    assert(instrumentNumber.getYear === 2013)
  }

  test("Parse BoP carpal tunnel syndrome citation") {
    assert(bopFixture.result.getCitation === "Statement of Principles concerning " +
      "carpal tunnel syndrome No. 8 of 2013")
  }

  test("Parse BoP carpal tunnel syndrome condition name") {
    assert(bopFixture.result.getConditionName === "carpal tunnel syndrome")
  }

  test("Parse BoP carpal tunnel syndrome effective from date") {
    assert(bopFixture.result.getEffectiveFromDate === LocalDate.of(2013, 1, 9))
  }

  test("Parse BoP carpal tunnel syndrome standard of proof") {
    assert(bopFixture.result.getStandardOfProof === StandardOfProof.BalanceOfProbabilities)
  }

  // ICD codes
  test("Parse BoP carpal tunnel syndrome ICD codes") {
    val icdCodes = bopFixture.result.getICDCodes
    assert(icdCodes.size() === 1)
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "G56.0")))
  }

  // Onset factors
  ignore("Parse BoP carpal tunnel syndrome onset factors") {
    val a = new ParsedFactor("6(a)",
      "performing any combination of repetitive activities or forceful activities with the " +
        "affected hand for at least 260 hours within a period of 210 consecutive days before " +
        "the clinical onset of carpal tunnel syndrome, and where the repetitive or forceful " +
        "activities have not ceased more than 30 days before the clinical onset of carpal " +
        "tunnel syndrome",
      Set(forcefulDef, repetitiveActivitiesDef))

    val b = new ParsedFactor("6(b)",
      "daily self-propulsion of a manual wheelchair for at least a cumulative period of 260 " +
        "hours within a period of 210 consecutive days before the clinical onset of carpal " +
        "tunnel syndrome, and where the self-propulsion of a manual wheelchair has not ceased " +
        "more than 30 days before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val c = new ParsedFactor("6(c)",
      "performing activities where the affected hand or forearm is directly vibrated for " +
        "at least 260 hours within a period of 210 consecutive days before the clinical " +
        "onset of carpal tunnel syndrome, and where those activities have not ceased more " +
        "than 30 days before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val d = new ParsedFactor("6(d)",
      "(d) having an injury to the affected wrist or hand which does not involve a fracture or " +
        "a dislocation but which:\n(i) alters the normal contour of the carpal tunnel; or\n" +
        "(ii) damages the flexor tendons within the carpal tunnel,\nwithin the one year " +
        "before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val e = new ParsedFactor("6(e)",
      "(e) having a fracture or dislocation to the distal radius, the distal ulna, a carpal bone " +
        "or a metacarpal bone of the affected side which:\n(i) alters the normal contour of " +
        "the carpal tunnel; or\n(ii) damages the flexor tendons within the carpal tunnel,\n" +
        "before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val f = new ParsedFactor("6(f)",
      "(f) having surgery to the affected wrist or hand which:\n(i) alters the normal contour of " +
        "the carpal tunnel; or\n(ii) damages the flexor tendons within the carpal tunnel,\n" +
        "within the one year before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val g = new ParsedFactor("6(g)",
      "being obese at the time of the clinical onset of carpal tunnel syndrome",
      Set(obeseDef))

    val h = new ParsedFactor("6(h)",
      "undergoing haemodialysis or peritoneal dialysis for at least the one year before the " +
        "clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val i = new ParsedFactor("6(i)",
      "having hypothyroidism at the time of the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val j = new ParsedFactor("6(j)",
      "having acromegaly before the clinical onset of carpal tunnel syndrome",
      Set(acromegalyDef))

    val k = new ParsedFactor("6(k)",
      "having amyloidosis at the time of the clinical onset of carpal tunnel syndrome",
      Set(amyloidosisDef))

    val l = new ParsedFactor("6(l)",
      "having gout in the affected wrist or hand at the time of the clinical onset " +
        "of carpal tunnel syndrome",
      Nil.toSet)

    val m = new ParsedFactor("6(m)",
      "having a space-occupying lesion in the affected carpal tunnel at the time of " +
        "the clinical onset of carpal tunnel syndrome",
      Set(lesionDef))

    val n = new ParsedFactor("6(n)",
      "having oedema involving the affected carpal tunnel at the time of the clinical " +
        "onset of carpal tunnel syndrome",
      Set(oedemaDef))

    val o = new ParsedFactor("6(o)",
      "having an external burn involving the affected wrist or palm requiring hospitalisation " +
        "within the five years before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val p = new ParsedFactor("6(p)",
      "having haemorrhage involving the affected carpal tunnel at the time of the clinical " +
        "onset of carpal tunnel syndrome",
      Nil.toSet)

    val q = new ParsedFactor("6(q)",
      "having infection involving the affected carpal tunnel at the time of the clinical onset " +
        "of carpal tunnel syndrome",
      Nil.toSet)

    val r = new ParsedFactor("6(r)",
      "having inflammatory arthritis of the affected wrist or hand at the time of the clinical " +
        "onset of carpal tunnel syndrome",
      Set(arthritisDef))

    val s = new ParsedFactor("6(s)",
      "having osteoarthritis of the affected wrist, carpus or trapeziometacarpal joint " +
        "of the thumb at the time of the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val t = new ParsedFactor("6(t)",
      "having diabetes mellitus at the time of the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val u = new ParsedFactor("6(u)",
      "being treated with an aromatase inhibitor within the six months before the " +
        "clinical onset of carpal tunnel syndrome",
      Set(aromataseDef))

    val v = new ParsedFactor("6(v)",
      "being pregnant within the three months before the clinical onset of carpal tunnel syndrome",
      Nil.toSet)

    val onsetFactors = rhFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 22)
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
    assert(onsetFactors.contains(q))
    assert(onsetFactors.contains(r))
    assert(onsetFactors.contains(s))
    assert(onsetFactors.contains(t))
    assert(onsetFactors.contains(u))
    assert(onsetFactors.contains(v))
  }

  // Aggravation factors
  test("Parse BoP carpal tunnel syndrome aggravation factors") {
    val w = new ParsedFactor("6(w)",
      "performing any combination of repetitive activities or forceful activities with the " +
        "affected hand for at least 260 hours within a period of 210 consecutive days before " +
        "the clinical worsening of carpal tunnel syndrome, and where the repetitive or forceful " +
        "activities have not ceased more than 30 days before the clinical worsening of carpal " +
        "tunnel syndrome",
      Set(forcefulDef, repetitiveActivitiesDef))

    val x = new ParsedFactor("6(x)",
      "daily self-propulsion of a manual wheelchair for at least a cumulative period of 260 " +
        "hours within a period of 210 consecutive days before the clinical worsening of carpal " +
        "tunnel syndrome, and where the selfpropulsion of a manual wheelchair has not ceased " +
        "more than 30 days before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val y = new ParsedFactor("6(y)",
      "performing activities where the affected hand or forearm is directly vibrated for at " +
        "least 260 hours within a period of 210 consecutive days before the clinical worsening " +
        "of carpal tunnel syndrome, and where those activities have not ceased more than 30 " +
        "days before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val z = new ParsedFactor("6(z)",
      "having an injury to the affected wrist or hand which does not involve a fracture or " +
        "a dislocation but which:\n(i) alters the normal contour of the carpal tunnel; or\n" +
        "(ii) damages the flexor tendons within the carpal tunnel,\nwithin the one year " +
        "before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val aa = new ParsedFactor("6(aa)",
      "having a fracture or dislocation to the distal radius, the distal ulna, a carpal bone " +
        "or a metacarpal bone of the affected side which:\n(i) alters the normal contour of the " +
        "carpal tunnel; or\n(ii) damages the flexor tendons within the carpal tunnel,\n" +
        "before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val bb = new ParsedFactor("6(bb)",
      "having surgery to the affected wrist or hand which:\n(i) alters the normal contour of " +
        "the carpal tunnel; or\n(ii) damages the flexor tendons within the carpal tunnel,\n" +
        "within the one year before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val cc = new ParsedFactor("6(cc)",
      "being obese at the time of the clinical worsening of carpal tunnel syndrome",
      Set(obeseDef))

    val dd = new ParsedFactor("6(dd)",
      "undergoing haemodialysis or peritoneal dialysis for at least the one year before " +
        "the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val ee = new ParsedFactor("6(ee)",
      "having hypothyroidism at the time of the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val ff = new ParsedFactor("6(ff)",
      "having acromegaly before the clinical worsening of carpal tunnel syndrome",
      Set(acromegalyDef))

    val gg = new ParsedFactor("6(gg)",
      "having amyloidosis at the time of the clinical worsening of carpal tunnel syndrome",
      Set(amyloidosisDef))

    val hh = new ParsedFactor("6(hh)",
      "having gout in the affected wrist or hand at the time of the clinical " +
        "worsening of carpal tunnel syndrome",
      Nil.toSet)

    val ii = new ParsedFactor("6(ii)",
      "having a space-occupying lesion in the affected carpal tunnel at the time " +
        "of the clinical worsening of carpal tunnel syndrome",
      Set(lesionDef))

    val jj = new ParsedFactor("6(jj)",
      "having oedema involving the affected carpal tunnel at the time of the clinical " +
        "worsening of carpal tunnel syndrome",
      Set(oedemaDef))

    val kk = new ParsedFactor("6(kk)",
      "having an external burn involving the affected wrist or palm requiring " +
        "hospitalisation within the five years before the clinical worsening of " +
        "carpal tunnel syndrome",
      Nil.toSet)

    val ll = new ParsedFactor("6(ll)",
      "having haemorrhage involving the affected carpal tunnel at the time of the " +
        "clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val mm = new ParsedFactor("6(mm)",
      "having infection involving the affected carpal tunnel at the time of the " +
        "clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val nn = new ParsedFactor("6(nn)",
      "having inflammatory arthritis of the affected wrist or hand at the time of " +
        "the clinical worsening of carpal tunnel syndrome",
      Set(arthritisDef))

    val oo = new ParsedFactor("6(oo)",
      "having osteoarthritis of the affected wrist, carpus or trapeziometacarpal joint " +
        "of the thumb at the time of the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val pp = new ParsedFactor("6(pp)",
      "having diabetes mellitus at the time of the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val qq = new ParsedFactor("6(qq)",
      "being treated with an aromatase inhibitor within the six months before the clinical " +
        "worsening of carpal tunnel syndrome",
      Set(aromataseDef))

    val rr = new ParsedFactor("6(rr)",
      "being pregnant within the three months before the clinical worsening of carpal tunnel syndrome",
      Nil.toSet)

    val ss = new ParsedFactor("6(ss)",
      "inability to obtain appropriate clinical management for carpal tunnel syndrome",
      Nil.toSet)

    val aggravationFactors = rhFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 23)
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
    assert(aggravationFactors.contains(gg))
    assert(aggravationFactors.contains(hh))
    assert(aggravationFactors.contains(ii))
    assert(aggravationFactors.contains(jj))
    assert(aggravationFactors.contains(kk))
    assert(aggravationFactors.contains(ll))
    assert(aggravationFactors.contains(mm))
    assert(aggravationFactors.contains(nn))
    assert(aggravationFactors.contains(oo))
    assert(aggravationFactors.contains(pp))
    assert(aggravationFactors.contains(qq))
    assert(aggravationFactors.contains(rr))
    assert(aggravationFactors.contains(ss))
  }

}
