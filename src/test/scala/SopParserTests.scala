
package au.gov.dva.sopapi.tests.parsers;

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities._
import au.gov.dva.sopapi.sopref.parsing.implementations._
import com.google.common.io.Resources
import org.scalatest.{FlatSpec, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.io.Source


@RunWith(classOf[JUnitRunner])
class SopParserTests extends FunSuite {
  test("Clense LS raw text") {
    val rawText = ParserTestUtils.resourceToString("lsConvertedToText.txt");
    val result = GenericClenser.clense(rawText)

    assert(result.length() > 0)
    System.out.println("START:")
    System.out.print(result)
    System.out.println("END")
  }

  test("Extract Lumbar Spondylosis factors section from clensed text") {
    val testInput = ParserTestUtils.resourceToString("lsClensedText.txt")
    val underTest = new LsExtractor()
    val result = underTest.extractFactorSection(testInput)
    System.out.print(result);
    assert(result._1 == 6)
  }

  test("Extract definition section for Lumbar Spondylosis") {
    val testInput = ParserTestUtils.resourceToString("lsClensedText.txt")
    val underTest = new LsExtractor()
    val result = underTest.extractDefinitionsSection(testInput);
    assert(result.startsWith("For the purpose") && result.endsWith("surgery to the lumbar spine."))
    System.out.print(result)
  }

  test("Extract date of effect for Lumbar Spondylosis") {
    val testInput = ParserTestUtils.resourceToString("lsClensedText.txt")
    val underTest = new LsExtractor()
    val result = underTest.extractDateOfEffectSection(testInput);
    assert(result == "This Instrument takes effect from 2 July 2014.");
  }

  test("Extract citation for Lumbar Spondylosis") {
    val testInput = ParserTestUtils.resourceToString("lsClensedText.txt")
    val underTest = new LsExtractor()
    val result = underTest.extractCitation(testInput);
    assert(result == "This Instrument may be cited as Statement of Principles concerning lumbar spondylosis No. 62 of 2014.");
  }


  test("Extract ICD codes for Lumbar Spondylosis") {
    val testInput = ParserTestUtils.resourceToString("lsClensedText.txt")
    val underTest = new LsExtractor()
    val result = underTest.extractICDCodes(testInput);
    result.foreach(c => System.out.print(c))
    assert(result.size == 9)
  }

  test("Parse single factor") {
    val testInput = "(a) being a prisoner of war before the clinical onset of lumbar spondylosis; or ";
    val undertest = LsParser
    val result = undertest.parseAll(undertest.singleParaParser, testInput)
    System.out.print(result)
  }


  test("Parse all factors from Lumbar Spondylosis"){
    val testInput = ParserTestUtils.resourceToString("lsExtractedFactorsText.txt")
    val underTest = LsParser;
    val result = underTest.parseAll(underTest.completeFactorSectionParser,testInput)
    System.out.print(result)
    assert(result.successful)
     assert(result.get._2.size == 32)
  }

  test("Ls parser implements interface correctly") {

    val testInput = ParserTestUtils.resourceToString("lsExtractedFactorsText.txt")
    val underTest = LsParser;
    val result = underTest.parseFactors(testInput)
    assert(result._1 == StandardOfProof.ReasonableHypothesis)
    assert(result._2.size == 32)
  }

  test("Parse instrument number") {
    val testInput = "This Instrument may be cited as Statement of Principles concerning lumbar spondylosis No. 62 of 2014."
    val result = LsParser.parseInstrumentNumber(testInput)
    assert(result.getNumber == 62 && result.getYear == 2014)

  }

  test("Divide definitions section to individual definitions") {
    val testInput = ParserTestUtils.resourceToString("lsExtractedDefinitionsSection.txt")
    val result = DefinitionsParsers.splitToDefinitions(testInput)
    assert(result.size == 17 && result.drop(1).forall(s => s.endsWith(";")))
  }

  test("Parse single definition section") {
    val testInput = "\"trauma to the lumbar spine\" means a discrete event involving the\napplication of significant physical force, including G force, to the lumbar spine\nthat causes the development within twenty-four hours of the injury being\nsustained, of symptoms and signs of pain and tenderness and either altered\nmobility or range of movement of the lumbar spine. In the case of sustained\nunconsciousness or the masking of pain by analgesic medication, these\nsymptoms and signs must appear on return to consciousness or the withdrawal\nof the analgesic medication. These symptoms and signs must last for a period\nof at least seven days following their onset; save for where medical\nintervention has occurred and that medical intervention involves either:\n(a) immobilisation of the lumbar spine by splinting, or similar external\nagent;\n(b) injection of corticosteroids or local anaesthetics into the lumbar spine; or\n(c) surgery to the lumbar spine."

    val result = DefinitionsParsers.parseSingleDefinition(testInput)
    assert(result._1 == "trauma to the lumbar spine" && result._2.startsWith("means a ") && result._2.endsWith("lumbar spine"))
  }

  test("Parse LS definitions section") {
    val testInput = ParserTestUtils.resourceToString("lsExtractedDefinitionsSection.txt")
    val result = LsParser.parseDefinitions(testInput)
    assert(result.size == 17)
  }

  test("Parse date of effect") {
    val dateOfEffectSection = "This Instrument takes effect from 2 July 2014."
    val result = LsParser.parseDateOfEffect(dateOfEffectSection)
    assert(result.getYear == 2014)
  }

  test("Parse entire LS SoP") {
      val testInput = ParserTestUtils.resourceToString("lsClensedText.txt")
      val result: SoP = LsSoPFactory.create("F2014L00933",testInput)
      val asJson = StoredSop.toJson(result)
      System.out.print(TestUtils.prettyPrint(asJson))
      assert(result != null)
  }

  test("Extract aggravation section from LS SoP")
  {

    val testInput = ParserTestUtils.resourceToString("lsClensedText.txt")
    val undertest = new LsExtractor
    val result =undertest.extractAggravationSection(testInput)
    assert(result == "Paragraphs 6(q) to 6(ff) applies only to material contribution to, or aggravation of, lumbar spondylosis where the person’s lumbar spondylosis was suffered or contracted before or during (but not arising out of) the person’s relevant service.")
  }


  test("Parse start and end of aggravation paras for LS") {
    val testInput = "Paragraphs 6(q) to 6(ff) applies only to material contribution to, or aggravation of, lumbar spondylosis where the person’s lumbar spondylosis was suffered or contracted before or during (but not arising out of) the person’s relevant service."

    val result = LsParser.parseStartAndEndAggravationParas(testInput)
    assert(result._1 == "(q)" && result._2 == "(ff)")
  }

  test("Divide factors into onset and aggravation") {
    val testData = List("(a)","(b)","(c)","(d)","(e)")
    val result = LsSoPFactory.splitFactors(testData,"(b)","(d)")
    assert(result == (List("(a)","(e)"),List("(b)","(c)","(d)")));
  }

  test("Get citation from citation section") {
    val input = "This Instrument may be cited as Statement of Principles concerning lumbar spondylosis No. 62 of 2014."
    val result = LsParser.parseCitation(input)
    assert(result == "Statement of Principles concerning lumbar spondylosis No. 62 of 2014" )
  }

  test("Get condition name from citation")
  {
    val input =  "This Instrument may be cited as Statement of Principles concerning lumbar spondylosis No. 62 of 2014."
    val result = LsParser.parseConditionNameFromCitation(input);
    assert(result == "lumbar spondylosis")
  }

  test("Parse entire RH LS SoP") {
    val testInput = ParserTestUtils.resourceToString("lsClensedText.txt")
    val result: SoP = LsSoPFactory.create("F2014L00933",testInput)
    val asJson = StoredSop.toJson(result)
    System.out.print(TestUtils.prettyPrint(asJson))
    assert(result != null)
  }

  test("Clense LS BoP text")
  {
    val rawText = ParserTestUtils.resourceToString("lsBopExtractedText.txt");
    val result = GenericClenser.clense(rawText)
    assert(result.length() > 0)
    System.out.println("START:")
    System.out.print(result)
    System.out.println("END")
  }

  test("Divide LS BoP to sections") {
    val sectionHeaderLineRegex = """^([0-9]+)\.\s""".r
    val clensedText = ParserTestUtils.resourceToString("lsBopClensedText.txt")
    val result = SoPExtractorUtilities.getSections(clensedText,sectionHeaderLineRegex)
    result.foreach(s => System.out.println("\n\n" + s))

    val split = result.map(s =>  SoPExtractorUtilities.parseSectionBlock(s))
    split.foreach(s => System.out.println(s))
    assert(result.size == 12)
  }

  test("Test get factors section with better approach")
  {
    val factorsRegex = """^Factors$""".r
    val clensedText = ParserTestUtils.resourceToString("lsBopClensedText.txt")
    val result = SoPExtractorUtilities.getSection(clensedText,factorsRegex)
    assert(result != null)
    System.out.print(result)
  }
}

