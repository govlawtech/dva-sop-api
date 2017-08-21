
package au.gov.dva.sopapi.tests.parsers;

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities._
import au.gov.dva.sopapi.sopref.parsing.implementations._
import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers.GenericClenser
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.{FactorsParser, PreAug2015DefinitionsParsers, PreAugust2015Parser}
import au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories.PreAug2015SoPFactory
import au.gov.dva.sopapi.sopref.parsing.traits.{OldSoPStyleExtractor, PreAug2015FactorsParser}
import com.google.common.io.Resources
import org.scalatest.{FlatSpec, FunSuite, Matchers}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.io.Source
import scala.util.Properties



@RunWith(classOf[JUnitRunner])
class SopParserTests extends FunSuite  {

  test("Extract Lumbar Spondylosis factors section from cleansed text") {
    val testInput = ParserTestUtils.resourceToString("lsCleansedText.txt")
    val underTest = new OldSoPStyleExtractor(testInput)
    val result = underTest.extractFactorsSection(testInput)
    System.out.print(result)
    assert(result._1 == 6)
  }

  test("Extract definition section for Lumbar Spondylosis") {
    val testInput = ParserTestUtils.resourceToString("lsCleansedText.txt")
    val underTest = new OldSoPStyleExtractor(testInput)
    val result = underTest.extractDefinitionsSection(testInput);
    assert(result.startsWith("For the purpose") && result.endsWith("surgery to the lumbar spine."))
    System.out.print(result)
  }

  test("Extract date of effect for Lumbar Spondylosis") {
    val testInput = ParserTestUtils.resourceToString("lsCleansedText.txt")
    val underTest = new OldSoPStyleExtractor(testInput)
    val result = underTest.extractDateOfEffectSection(testInput);
    assert(result == "This Instrument takes effect from 2 July 2014.");
  }

  test("Extract citation for Lumbar Spondylosis") {
    val testInput = ParserTestUtils.resourceToString("lsCleansedText.txt")
    val underTest = new OldSoPStyleExtractor(testInput)
    val result = underTest.extractCitation(testInput);
    assert(result == "This Instrument may be cited as Statement of Principles concerning lumbar spondylosis No. 62 of 2014.");
  }


  test("Extract ICD codes for Lumbar Spondylosis") {
    val testInput = ParserTestUtils.resourceToString("lsCleansedText.txt")
    val underTest = new OldSoPStyleExtractor(testInput)

    val result = underTest.extractICDCodes(testInput);
    result.foreach(c => System.out.print(c))
    assert(result.size == 9)
  }


  test("Parse instrument number") {
    val testInput = "This Instrument may be cited as Statement of Principles concerning lumbar spondylosis No. 62 of 2014."
    val result = PreAugust2015Parser.parseInstrumentNumber(testInput)
    assert(result.getNumber == 62 && result.getYear == 2014)

  }

  test("Divide definitions section to individual definitions") {
    val testInput = ParserTestUtils.resourceToString("lsExtractedDefinitionsSection.txt")
    val result = PreAug2015DefinitionsParsers.splitToDefinitions(testInput)
    assert(result.size == 17 && result.drop(1).forall(s => s.endsWith(";")))
  }

  test("Parse single definition section") {
    val testInput = "\"trauma to the lumbar spine\" means a discrete event involving the application of significant physical force, including G force, to the lumbar spine that causes the development within twenty-four hours of the injury being sustained, of symptoms and signs of pain and tenderness and either altered mobility or range of movement of the lumbar spine. In the case of sustained unconsciousness or the masking of pain by analgesic medication, these symptoms and signs must appear on return to consciousness or the withdrawal of the analgesic medication. These symptoms and signs must last for a period of at least seven days following their onset; save for where medical intervention has occurred and that medical intervention involves either: (a) immobilisation of the lumbar spine by splinting, or similar external agent; (b) injection of corticosteroids or local anaesthetics into the lumbar spine; or (c) surgery to the lumbar spine."

    val result = PreAug2015DefinitionsParsers.parseSingleDefinition(testInput)
    assert(result._1 == "trauma to the lumbar spine" && result._2.startsWith("means a ") && result._2.endsWith("lumbar spine"))
  }

  test("Parse LS definitions section") {
    val testInput = ParserTestUtils.resourceToString("lsExtractedDefinitionsSection.txt")
    val result = PreAugust2015Parser.parseDefinitions(testInput)
    assert(result.size == 17)
  }

  test("Parse date of effect") {
    val dateOfEffectSection = "This Instrument takes effect from 2 July 2014."
    val result = PreAugust2015Parser.parseDateOfEffect(dateOfEffectSection)
    assert(result.getYear == 2014)
  }

  test("Parse entire LS SoP") {
    val cleansedText = ParserTestUtils.resourceToString("lsCleansedText.txt")
    val result: SoP = PreAug2015SoPFactory.create("F2014L00933", cleansedText)
    val asJson = StoredSop.toJson(result)
    System.out.print(TestUtils.prettyPrint(asJson))
    assert(result != null)
  }

  test("Extract aggravation section from LS SoP") {

    val testInput = ParserTestUtils.resourceToString("lsCleansedText.txt")
    val underTest = new OldSoPStyleExtractor(testInput)

    val result = underTest.extractAggravationSection(testInput)
    assert(result.get == "Paragraphs 6(q) to 6(ff) applies only to material contribution to, or aggravation of, lumbar spondylosis where the person’s lumbar spondylosis was suffered or contracted before or during (but not arising out of) the person’s relevant service.")
  }


  test("Parse start and end of aggravation paras for LS") {
    val testInput = "Paragraphs 6(q) to 6(ff) applies only to material contribution to, or aggravation of, lumbar spondylosis where the person’s lumbar spondylosis was suffered or contracted before or during (but not arising out of) the person’s relevant service."

    val result = PreAugust2015Parser.parseStartAndEndAggravationParas(testInput)
    assert(result._1 == "(q)" && result._2 == "(ff)")
  }

  test("Divide factors into onset and aggravation") {
    val testData = List("(a)", "(b)", "(c)", "(d)", "(e)")
    val result = PreAug2015SoPFactory.splitFactors(testData, "(b)", "(d)")
    assert(result == (List("(a)", "(e)"), List("(b)", "(c)", "(d)")));
  }

  test("Get citation from citation section") {
    val input = "This Instrument may be cited as Statement of Principles concerning lumbar spondylosis No. 62 of 2014."
    val result = PreAugust2015Parser.parseCitation(input)
    assert(result == "Statement of Principles concerning lumbar spondylosis No. 62 of 2014")
  }

  test("Get condition name from citation") {
    val input = "This Instrument may be cited as Statement of Principles concerning lumbar spondylosis No. 62 of 2014."
    val result = PreAugust2015Parser.parseConditionNameFromCitation(input);
    assert(result == "lumbar spondylosis")
  }

  test("Parse entire RH LS SoP") {
    val cleansedText = ParserTestUtils.resourceToString("lsCleansedText.txt")
    val result: SoP = PreAug2015SoPFactory.create("F2014L00933", cleansedText)
    val asJson = StoredSop.toJson(result)
    System.out.print(TestUtils.prettyPrint(asJson))
    assert(result != null)
  }

  test("Clense LS BoP text") {
    val rawText = ParserTestUtils.resourceToString("lsBopExtractedText.txt");
    val result = GenericClenser.clense(rawText)
    assert(result.length() > 0)
    System.out.println("START:")
    System.out.print(result)
    System.out.println("END")
  }

  test("Divide LS BoP to sections") {
    val sectionHeaderLineRegex = """^([0-9]+)\.\s""".r
    val cleansedText = ParserTestUtils.resourceToString("lsBopCleansedText.txt")
    val result = SoPExtractorUtilities.getSections(cleansedText, sectionHeaderLineRegex)
    result.foreach(s => System.out.println("  " + s))

    val split = result.map(s => SoPExtractorUtilities.parseSectionBlock(s))
    split.foreach(s => System.out.println(s))
    assert(result.size == 12)
  }

  test("Test get factors section with better approach") {
    val sectionHeaderLineRegex = """^([0-9]+)\.\s""".r
    val factorsRegex = """^Factors$""".r
    val cleansedText = ParserTestUtils.resourceToString("lsBopCleansedText.txt")
    val result = SoPExtractorUtilities.getSection(cleansedText, factorsRegex)
    assert(result != null)
    System.out.print(result)
  }

  test("Take until letter appears a number of times") {
    val input = List("(hh) blah", "(i) blah blah", "(ii) blah blah", "(iii)", "(iv)", "(ii) main para again", "(jj) main para")
    val result = SoPExtractorUtilities.splitWithSkip(input, 2, l => l.startsWith("(ii)"))
    assert(result._1.size == 5 && result._2.size == 2)
  }


  test("Extract aggravation paras where two paras are mentioned only")
  {
    val input = "Paragraphs 6(e) and 6(f) apply only to material contribution to, or aggravation of, herpes zoster where the person's herpes zoster was suffered or contracted before or during (but not arising out of) the person's relevant service."
    val underTest: (String, String) = PreAugust2015Parser.parseStartAndEndAggravationParas(input)
    assert(underTest._1 == "(e)" && underTest._2 == "(f)")
  }


  test("Extract factor from single factor section")
  {
    val input = "The factor that must exist before it can be said that, on the balance of\nprobabilities, ganglion or death from ganglion is connected with the\ncircumstances of a person's relevant service is inability to obtain appropriate\nclinical management for ganglion."
    val result = FactorsParser.extractFactorFromFactorSectionHead(input.split("\n").toList)
    assert(result.getText.startsWith("inability to obtain appropriate clinical management for ganglion"))
  }

}

