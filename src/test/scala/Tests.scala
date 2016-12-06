
import au.gov.dva.sopref.interfaces.model.StandardOfProof
import au.gov.dva.sopref.parsing.SoPExtractorUtilities._
import au.gov.dva.sopref.parsing._
import au.gov.dva.sopref.parsing.implementations.{DefinitionsParsers, GenericClenser, LsExtractor, LsParser}
import com.google.common.io.Resources
import org.scalatest.{FlatSpec, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class Tests extends FunSuite {
  test("example test") {
     val underTest = true;
    assert(underTest)
  }
}

@RunWith(classOf[JUnitRunner])
class ParserTests extends FunSuite {
  test("Clense LS raw text") {
    val sourceResourceStream = getClass().getResourceAsStream("lsConvertedToText.txt");
    val rawText = Source.fromInputStream(sourceResourceStream).mkString
    val lSClenser = new GenericClenser();
    val result = lSClenser.clense(rawText)

    assert(result.length() > 0)
    System.out.println("START:")
    System.out.print(result)
    System.out.println("END")
  }

  test("Extract Lumbar Spondylosis factors section from clensed text") {
    val testInput = Source.fromInputStream(getClass().getResourceAsStream("lsClensedText.txt")).mkString;
    val underTest = new LsExtractor()
    val result = underTest.extractFactorSection(testInput)
    System.out.print(result);
    assert(result._1 == 6)
  }

  test("Extract definition section for Lumbar Spondylosis") {
    val testInput = Source.fromInputStream(getClass().getResourceAsStream("lsClensedText.txt")).mkString;
    val underTest = new LsExtractor()
    val result = underTest.extractDefinitionsSection(testInput);
    assert(result.startsWith("For the purpose") && result.endsWith("surgery to the lumbar spine."))
    System.out.print(result)
  }

  test("Extract date of effect for Lumbar Spondylosis") {
    val testInput = Source.fromInputStream(getClass().getResourceAsStream("lsClensedText.txt")).mkString;
    val underTest = new LsExtractor()
    val result = underTest.extractDateOfEffectSection(testInput);
    assert(result == "This Instrument takes effect from 2 July 2014.");
  }

  test("Extract citation for Lumbar Spondylosis") {
    val testInput = Source.fromInputStream(getClass().getResourceAsStream("lsClensedText.txt")).mkString;
    val underTest = new LsExtractor()
    val result = underTest.extractCitation(testInput);
    assert(result == "This Instrument may be cited as Statement of Principles concerning lumbar spondylosis No. 62 of 2014.");
  }


  test("Extract ICD codes for Lumbar Spondylosis") {
    val testInput = Source.fromInputStream(getClass().getResourceAsStream("lsClensedText.txt")).mkString;
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

  test("Parse several factors") {
    val testinput = "(a) being a prisoner of war before the clinical onset of lumbar spondylosis; or (b) having inflammatory joint disease in the lumbar spine before the clinical onset of lumbar spondylosis; or (c) having an infection of the affected joint as specified at least one year before the clinical onset of lumbar spondylosis; or (d) having an intra-articular fracture of the lumbar spine at least one year before the clinical onset of lumbar spondylosis; or (e) having a specified spinal condition affecting the lumbar spine for at least the one year before the clinical onset of lumbar spondylosis; or (f) having leg length inequality for at least the two years before the clinical onset of lumbar spondylosis; or (g) having a depositional joint disease in the lumbar spine before the clinical onset of lumbar spondylosis; or ";

    val underTest =  LsParser
    val result = underTest.parseAll(underTest.factorListParser, testinput)
    System.out.print(result)
    assert(result.successful && result.get.size == 7)
  }

  test("Parse head and factors") {
    val testinput = "The factor that must as a minimum exist before it can be said that a reasonable hypothesis has been raised connecting lumbar spondylosis or death from lumbar spondylosis with the circumstances of a person’s relevant service is: (a) being a prisoner of war before the clinical onset of lumbar spondylosis; or (b) having inflammatory joint disease in the lumbar spine before the clinical onset of lumbar spondylosis; or (c) having an infection of the affected joint as specified at least one year before the clinical onset of lumbar spondylosis; or "

    val underTest = LsParser
    val result = underTest.parseAll(underTest.headAndFactorsParser, testinput)

    System.out.print(result)
    assert(result.successful && result.get._2.size == 3)

  }



  test("Parse all factors from Lumbar Spondylosis"){
    val testInput = Source.fromInputStream(getClass().getResourceAsStream("lsExtractedFactorsText.txt"),"UTF-8").mkString;
    val underTest = LsParser;
    val result = underTest.parseAll(underTest.completeFactorSectionParser,testInput)
    System.out.print(result)
    assert(result.successful)
     assert(result.get._2.size == 32)
  }

  test("Ls parser implements interface correctly") {

    val testInput = Source.fromInputStream(getClass().getResourceAsStream("lsExtractedFactorsText.txt"),"UTF-8").mkString;
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
    val testInput = Source.fromInputStream(getClass().getResourceAsStream("lsExtractedDefinitionsSection.txt"),"UTF-8").mkString;
    val result = DefinitionsParsers.splitToDefinitions(testInput)
    assert(result.size == 17 && result.drop(1).forall(s => s.endsWith(";")))
  }

  test("Parse single definition section") {
    val testInput = "\"trauma to the lumbar spine\" means a discrete event involving the\napplication of significant physical force, including G force, to the lumbar spine\nthat causes the development within twenty-four hours of the injury being\nsustained, of symptoms and signs of pain and tenderness and either altered\nmobility or range of movement of the lumbar spine. In the case of sustained\nunconsciousness or the masking of pain by analgesic medication, these\nsymptoms and signs must appear on return to consciousness or the withdrawal\nof the analgesic medication. These symptoms and signs must last for a period\nof at least seven days following their onset; save for where medical\nintervention has occurred and that medical intervention involves either:\n(a) immobilisation of the lumbar spine by splinting, or similar external\nagent;\n(b) injection of corticosteroids or local anaesthetics into the lumbar spine; or\n(c) surgery to the lumbar spine."

    val result = DefinitionsParsers.parseSingleDefinition(testInput)
    assert(result._1 == "trauma to the lumbar spine" && result._2.startsWith("means a ") && result._2.endsWith("lumbar spine."))
  }

  test("Parse LS definitions section") {
    val testInput = Source.fromInputStream(getClass().getResourceAsStream("lsExtractedDefinitionsSection.txt"),"UTF-8").mkString;
    val result = LsParser.parseDefinitions(testInput)
    assert(result.size == 17)
  }

  test("Parse date of effect") {
    val dateOfEffectSection = "This Instrument takes effect from 2 July 2014."
    val result = LsParser.parseDateOfEffect(dateOfEffectSection)
    assert(result.getYear == 2014)
  }
}

