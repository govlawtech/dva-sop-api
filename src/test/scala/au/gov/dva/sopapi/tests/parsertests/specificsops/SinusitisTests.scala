
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
class SinusitisTests extends FunSuite {

  val rhFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2010L00553", "sops_rh/F2010L00553.pdf")
  }

  val bopFixture = new {
    val result = ParserTestUtils.executeWholeParsingPipeline("F2010L00554", "sops_bop/F2010L00554.pdf")
  }

  val radiationDef = new ParsedDefinedTerm("a course of therapeutic radiation",
    "means one or more fractions\n(treatment portions) of ionising radiation " +
      "administered with the aim of\nachieving palliation or cure with gamma rays, " +
      "x-rays, alpha particles or\nbeta particles")

  val dentalDef = new ParsedDefinedTerm("a specified dental condition",
    "means one of the following conditions:\n(a) endosseous implants;\n(b) infected dental " +
      "(apical or dentigerous) cyst;\n(c) non-vital tooth;\n(d) oro-antral fistula;\n" +
      "(e) periapical abscess;\n(f) periapical granuloma; or\n(g) periodontal disease")

  val substanceDef = new ParsedDefinedTerm("a specified substance",
    "means mustard gas, lewisite, ammonia gas,\nchlorine gas, sulphur dioxide, " +
      "nitrogen dioxide or cocaine")

  val respiratoryDef = new ParsedDefinedTerm("a viral respiratory tract infection",
    "means an acute infection of the\nrespiratory epithelium by a range of viruses " +
      "including rhinovirus,\ncoronavirus, influenza virus, causing such illnesses " +
      "as the common cold,\nlaryngotracheobronchitis, tracheitis, bronchitis and pneumonia")

  val nasalDef = new ParsedDefinedTerm("acute nasal symptoms or signs",
    "means:\n(a) rhinorrhea; or\n(b) irritation, inflammation, oedema, ulceration or " +
      "haemorrhage of\nthe nasal mucosa")

  val immunocompromisedDef = new ParsedDefinedTerm("an immunocompromised state",
    "means a state where the immune\nresponse has been attenuated by administration " +
      "of immunosuppressive\ndrugs, irradiation, malnutrition, a malignant disease " +
      "process or certain\ntypes of infection")

  val cigsPerDayDef = new ParsedDefinedTerm("cigarettes per day, or the equivalent " +
    "thereof in other tobacco products",
    "means either cigarettes, pipe tobacco or cigars, alone or in\nany combination " +
      "where one tailor made cigarette approximates one gram\nof tobacco; or one gram " +
      "of cigar, pipe or other smoking tobacco")

  val drainageDef = new ParsedDefinedTerm("impaired drainage of the sinus",
    "means one of the following which\nleads to a narrowing or obstruction of the " +
      "affected sinus or sinus\nopening:\n(a) an anatomical deformity including deviated " +
      "septum, enlarged\nturbinates, adenoidal hypertrophy, fracture of the facial bones " +
      "or\nany other bony structural abnormalities;\n(b) a soft tissue abnormality or " +
      "mucosal swelling affecting the sinus\nincluding polyps, tumours, inflammation, " +
      "sarcoidosis,\ngranulomas, or scarring; or\n(c) a foreign body including nasal " +
      "packing, nasogastric or\nnasotracheal tubes, or dental detritus")

  val cigsPackYearDef = new ParsedDefinedTerm("pack year of cigarettes, or the equivalent " +
    "thereof in other tobacco products",
    "means a calculation of consumption where one pack year of\ncigarettes equals twenty " +
      "tailor made cigarettes per day for a period of\none calendar year, or 7300 cigarettes.  " +
      "One tailor made cigarette\napproximates one gram of tobacco or one gram of cigar or pipe " +
      "tobacco\nby weight.  One pack year of tailor made cigarettes equates to 7300\ncigarettes, " +
      "or 7.3 kg of smoking tobacco by weight.  Tobacco products\nmeans either cigarettes, pipe " +
      "tobacco or cigars smoked, alone or in any\ncombination")

  test("Parse entire RH sinusitis SoP") {
    System.out.print(TestUtils.prettyPrint(StoredSop.toJson(rhFixture.result)))
    assert(rhFixture.result != null)
  }

  test("Parse RH sinusitis register ID") {
    assert(rhFixture.result.getRegisterId === "F2010L00553")
  }

  test("Parse RH sinusitis instrument number") {
    val instrumentNumber = rhFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 9)
    assert(instrumentNumber.getYear === 2010)
  }

  test("Parse RH sinusitis citation") {
    assert(rhFixture.result.getCitation === "Statement of Principles concerning " +
      "sinusitis No. 9 of 2010")
  }

  test("Parse RH sinusitis condition name") {
    assert(rhFixture.result.getConditionName === "sinusitis")
  }

  test("Parse RH sinusitis effective from date") {
    assert(rhFixture.result.getEffectiveFromDate === LocalDate.of(2010, 3, 10))
  }

  test("Parse RH sinusitis standard of proof") {
    assert(rhFixture.result.getStandardOfProof === StandardOfProof.ReasonableHypothesis)
  }

  // ICD codes
  test("Parse RH sinusitis ICD codes") {
    val icdCodes = rhFixture.result.getICDCodes
    assert(icdCodes.size() === 2)
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "J01")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "J32")))
  }

  // Onset factors
  test("Parse RH sinusitis onset factors") {
    val a = new ParsedFactor("6(a)",
      "having a viral respiratory tract infection at the time of the clinical onset of sinusitis",
      Set(respiratoryDef))

    val b = new ParsedFactor("6(b)",
      "having impaired drainage of the sinus at the time of the clinical onset of sinusitis",
      Set(drainageDef))

    val c = new ParsedFactor("6(c)",
      "being infected with human immunodeficiency virus at the time of the clinical onset of sinusitis",
      Nil.toSet)

    val d = new ParsedFactor("6(d)",
      "being in an immunocompromised state at the time of the clinical onset of sinusitis",
      Set(immunocompromisedDef))

    val e = new ParsedFactor("6(e)",
      "having diabetes mellitus at the time of the clinical onset of sinusitis",
      Nil.toSet)

    val f = new ParsedFactor("6(f)",
      "inhaling a specified substance which results in:\r\n(i) acute nasal symptoms or signs within " +
        "48 hours of the inhalation; and\r\n(ii) scarring or erosion of the nasal or sinus mucosa,\r\n" +
        "before the clinical onset of sinusitis",
      Set(nasalDef, substanceDef))

    val g = new ParsedFactor("6(g)",
      "for sinusitis affecting the maxillary sinus only, having a specified dental condition " +
        "affecting the tissues adjacent to the affected maxillary sinus at the time of the " +
        "clinical onset of sinusitis",
      Set(dentalDef))

    val h = new ParsedFactor("6(h)",
      "having allergic rhinitis at the time of the clinical onset of sinusitis",
      Nil.toSet)

    val i = new ParsedFactor("6(i)",
      "having sinus barotrauma at the time of the clinical onset of sinusitis",
      Nil.toSet)

    val j = new ParsedFactor("6(j)",
      "undergoing a course of therapeutic radiation to the head within the six " +
        "weeks before the clinical onset of sinusitis",
      Set(radiationDef))

    val k = new ParsedFactor("6(k)",
      "smoking on average at least ten cigarettes per day, or the equivalent thereof in " +
        "other tobacco products and having smoked at least one pack year of cigarettes, or " +
        "the equivalent thereof in other tobacco products, at the time of the clinical onset " +
        "of sinusitis",
      Set(cigsPerDayDef, cigsPackYearDef))

    val l = new ParsedFactor("6(l)",
      "having gastroesophageal reflux disease at the time of the clinical onset of sinusitis",
      Nil.toSet)

    val onsetFactors = rhFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 12)
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
  }

  // Aggravation factors
  test("Parse RH sinusitis aggravation factors") {
    val m = new ParsedFactor("6(m)",
      "having a viral respiratory tract infection at the time of the clinical worsening of sinusitis",
      Set(respiratoryDef))

    val n = new ParsedFactor("6(n)",
      "having impaired drainage of the sinus at the time of the clinical worsening of sinusitis",
      Set(drainageDef))

    val o = new ParsedFactor("6(o)",
      "being infected with human immunodeficiency virus at the time of the " +
        "clinical worsening of sinusitis",
      Nil.toSet)

    val p = new ParsedFactor("6(p)",
      "being in an immunocompromised state at the time of the clinical worsening of sinusitis",
      Set(immunocompromisedDef))

    val q = new ParsedFactor("6(q)",
      "having diabetes mellitus at the time of the clinical worsening of sinusitis",
      Nil.toSet)

    val r = new ParsedFactor("6(r)",
      "inhaling a specified substance which results in:\r\n(i) acute nasal symptoms or signs " +
        "within 48 hours of the inhalation; and\r\n(ii) scarring or erosion of the nasal or " +
        "sinus mucosa,\r\nbefore the clinical worsening of sinusitis",
      Set(nasalDef, substanceDef))

    val s = new ParsedFactor("6(s)",
      "for sinusitis affecting the maxillary sinus only, having a specified dental condition " +
        "affecting the tissues adjacent to the affected maxillary sinus at the time " +
        "of the clinical worsening of sinusitis",
      Set(dentalDef))

    val t = new ParsedFactor("6(t)",
      "having allergic rhinitis at the time of the clinical worsening of sinusitis",
      Nil.toSet)

    val u = new ParsedFactor("6(u)",
      "having sinus barotrauma at the time of the clinical worsening of sinusitis",
      Nil.toSet)

    val v = new ParsedFactor("6(v)",
      "undergoing a course of therapeutic radiation to the head within the " +
        "six weeks before the clinical worsening of sinusitis",
      Set(radiationDef))

    val w = new ParsedFactor("6(w)",
      "smoking on average at least ten cigarettes per day, or the equivalent thereof in " +
        "other tobacco products and having smoked at least one pack year of cigarettes, " +
        "or the equivalent thereof in other tobacco products, at the time of the " +
        "clinical worsening of sinusitis",
      Set(cigsPerDayDef, cigsPackYearDef))

    val x = new ParsedFactor("6(x)",
      "having gastroesophageal reflux disease at the time of the clinical worsening of sinusitis",
      Nil.toSet)

    val y = new ParsedFactor("6(y)",
      "inability to obtain appropriate clinical management for sinusitis",
      Nil.toSet)

    val aggravationFactors = rhFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 13)
    assert(aggravationFactors.contains(m))
    assert(aggravationFactors.contains(n))
    assert(aggravationFactors.contains(o))
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
  }

  test("Parse entire BoP sinusitis SoP")
  {
    System.out.println(TestUtils.prettyPrint(StoredSop.toJson(bopFixture.result)))
    assert(bopFixture.result != null)
  }

  test("Parse BoP sinusitis register ID") {
    assert(bopFixture.result.getRegisterId === "F2010L00554")
  }

  test("Parse BoP sinusitis instrument number") {
    val instrumentNumber = bopFixture.result.getInstrumentNumber
    assert(instrumentNumber.getNumber === 10)
    assert(instrumentNumber.getYear === 2010)
  }

  test("Parse BoP sinusitis citation") {
    assert(bopFixture.result.getCitation === "Statement of Principles concerning " +
      "sinusitis No. 10 of 2010")
  }

  test("Parse BoP sinusitis condition name") {
    assert(bopFixture.result.getConditionName === "sinusitis")
  }

  test("Parse BoP sinusitis effective from date") {
    assert(bopFixture.result.getEffectiveFromDate === LocalDate.of(2010, 3, 10))
  }

  test("Parse BoP sinusitis standard of proof") {
    assert(bopFixture.result.getStandardOfProof === StandardOfProof.BalanceOfProbabilities)
  }

  // ICD codes
  test("Parse BoP sinusitis ICD codes") {
    val icdCodes = bopFixture.result.getICDCodes
    assert(icdCodes.size() === 2)
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "J01")))
    assert(icdCodes.contains(new BasicICDCode("ICD-10-AM", "J32")))
  }

  // Onset factors
  test("Parse BoP sinusitis onset factors") {
    val a = new ParsedFactor("6(a)",
      "having a viral respiratory tract infection at the time of the clinical onset of sinusitis",
      Set(respiratoryDef))

    val b = new ParsedFactor("6(b)",
      "having impaired drainage of the sinus at the time of the clinical onset of sinusitis",
      Set(drainageDef))

    val c = new ParsedFactor("6(c)",
      "being infected with human immunodeficiency virus at the time of the clinical onset of sinusitis",
      Nil.toSet)

    val d = new ParsedFactor("6(d)",
      "being in an immunocompromised state at the time of the clinical onset of sinusitis",
      Set(immunocompromisedDef))

    val e = new ParsedFactor("6(e)",
      "having diabetes mellitus at the time of the clinical onset of sinusitis",
      Nil.toSet)

    val f = new ParsedFactor("6(f)",
      "inhaling a specified substance which results in:\r\n(i) acute nasal symptoms or signs " +
        "within 48 hours of the inhalation; and\r\n(ii) scarring or erosion of the nasal or " +
        "sinus mucosa, before the clinical onset of sinusitis",
      Set(nasalDef, substanceDef))

    val g = new ParsedFactor("6(g)",
      "for sinusitis affecting the maxillary sinus only, having a specified dental condition " +
        "affecting the tissues adjacent to the affected maxillary sinus at the time of " +
        "the clinical onset of sinusitis",
      Set(dentalDef))

    val h = new ParsedFactor("6(h)",
      "having allergic rhinitis at the time of the clinical onset of sinusitis",
      Nil.toSet)

    val i = new ParsedFactor("6(i)",
      "having sinus barotrauma at the time of the clinical onset of sinusitis",
      Nil.toSet)

    val j = new ParsedFactor("6(j)",
      "undergoing a course of therapeutic radiation to the head within the six weeks " +
        "before the clinical onset of sinusitis",
      Set(radiationDef))

    val onsetFactors = bopFixture.result.getOnsetFactors
    assert(onsetFactors.size() === 10)
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
  }

  // Aggravation factors
  test("Parse BoP sinusitis aggravation factors") {
    val k = new ParsedFactor("6(k)",
      "having a viral respiratory tract infection at the time of the clinical worsening of sinusitis",
      Set(respiratoryDef))

    val l = new ParsedFactor("6(l)",
      "having impaired drainage of the sinus at the time of the clinical worsening of sinusitis",
      Set(drainageDef))

    val m = new ParsedFactor("6(m)",
      "being infected with human immunodeficiency virus at the time of the clinical worsening of sinusitis",
      Nil.toSet)

    val n = new ParsedFactor("6(n)",
      "being in an immunocompromised state at the time of the clinical worsening of sinusitis",
      Set(immunocompromisedDef))

    val o = new ParsedFactor("6(o)",
      "having diabetes mellitus at the time of the clinical worsening of sinusitis",
      Nil.toSet)

    val p = new ParsedFactor("6(p)",
      "inhaling a specified substance which results in:\r\n(i) acute nasal symptoms or " +
        "signs within 48 hours of the inhalation; and\r\n(ii) scarring or erosion of the " +
        "nasal or sinus mucosa,\r\nbefore the clinical worsening of sinusitis",
      Set(nasalDef, substanceDef))

    val q = new ParsedFactor("6(q)",
      "for sinusitis affecting the maxillary sinus only, having a specified dental " +
        "condition affecting the tissues adjacent to the affected maxillary sinus at " +
        "the time of the clinical worsening of sinusitis",
      Set(dentalDef))

    val r = new ParsedFactor("6(r)",
      "having allergic rhinitis at the time of the clinical worsening of sinusitis",
      Nil.toSet)

    val s = new ParsedFactor("6(s)",
      "having sinus barotrauma at the time of the clinical worsening of sinusitis",
      Nil.toSet)

    val t = new ParsedFactor("6(t)",
      "undergoing a course of therapeutic radiation to the head within the six " +
        "weeks before the clinical worsening of sinusitis",
      Set(radiationDef))

    val u = new ParsedFactor("6(u)",
      "inability to obtain appropriate clinical management for sinusitis",
      Nil.toSet)

    val aggravationFactors = bopFixture.result.getAggravationFactors
    assert(aggravationFactors.size() === 11)
    assert(aggravationFactors.contains(k))
    assert(aggravationFactors.contains(l))
    assert(aggravationFactors.contains(m))
    assert(aggravationFactors.contains(n))
    assert(aggravationFactors.contains(o))
    assert(aggravationFactors.contains(p))
    assert(aggravationFactors.contains(q))
    assert(aggravationFactors.contains(r))
    assert(aggravationFactors.contains(s))
    assert(aggravationFactors.contains(t))
    assert(aggravationFactors.contains(u))
  }

}
