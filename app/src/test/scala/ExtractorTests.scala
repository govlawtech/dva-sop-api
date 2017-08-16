package au.gov.dva.sopapi.tests.parsers

import au.gov.dva.sopapi.sopref.data.Conversions
import au.gov.dva.sopapi.sopref.parsing.{PostAug2015ExtractorUtilities, SoPExtractorUtilities}
import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers.{GenericClenser, PostAug2015Clenser}
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.PostAug2015Extractor
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class ExtractorTests extends FunSuite{

  test("Split post Aug 2015 to sections")
  {
    val rawText = Conversions.pdfToPlainText(ParserTestUtils.resourceToBytes("sops_rh/F2017L00004.pdf"))
    val cleansedWithdefault = GenericClenser.clense(rawText)
    val sections: List[List[String]] = PostAug2015ExtractorUtilities.getSections(cleansedWithdefault)
    val parsedSections = sections.map( s => PostAug2015ExtractorUtilities.parseSectionBlock(s))
    assert(parsedSections.size > 0)
    println(parsedSections)
  }

  test("Post 2015 extractor")
  {
    val rawText = Conversions.pdfToPlainText(ParserTestUtils.resourceToBytes("sops_rh/F2017L00004.pdf"))
    val cleansedWithdefault = PostAug2015Clenser.clense(rawText)
    val underTest = new PostAug2015Extractor(cleansedText = cleansedWithdefault)
    val factorSection = underTest.extractFactorsSection(cleansedWithdefault)
    println(factorSection)
    assert(factorSection._1 == 9 && factorSection._2.endsWith("inability to obtain appropriate clinical management for haemorrhoids."))
  }

  test("Post 2015 extractor definitions")
  {
    val rawText = Conversions.pdfToPlainText(ParserTestUtils.resourceToBytes("sops_rh/F2017L00004.pdf"))
    val cleansedWithdefault = GenericClenser.clense(rawText)
    val underTest = new PostAug2015Extractor(cleansedText = cleansedWithdefault)
    val defs = underTest.extractDefinitionsSection(cleansedWithdefault)
    println(defs)
  }




}
