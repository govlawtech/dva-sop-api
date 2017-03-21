package au.gov.dva.sopapi.tests.parsers

import au.gov.dva.sopapi.sopref.data.Conversions
import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers.{GenericCleanser, PostAug2015Cleanser}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.util.Properties
;

@RunWith(classOf[JUnitRunner])
class CleanserTests extends FunSuite{

  test("Cleanse LS raw text") {
    val rawText = ParserTestUtils.resourceToString("lsConvertedToText.txt");
    val result = GenericCleanser.cleanse(rawText)

    assert(result.length() > 0)
    System.out.println("START:")
    System.out.print(result)
    System.out.println("END")
  }

  test("Reinstate exponents")
  {
    val input = "4G = 4 x 9.81m/s\n2"
    val replaced = """m/s[\r\n]+2""".r.replaceAllIn(input,"m/s\u00B2")
    println(replaced)
  }


  test("Cleanse notes in post Aug 2015 sops")
  {
    val rawText = Conversions.pdfToPlainText(ParserTestUtils.resourceToBytes("sops_rh/F2017L00004.pdf"))
    val cleansedWithdefault = GenericCleanser.cleanse(rawText)
    assert(!cleansedWithdefault.contains("Note: "))
    println(cleansedWithdefault)
  }

  test("Cleanse compilation footnotes")
  {
    val rawTextFromAnxietyDisorder = Conversions.pdfToPlainText(ParserTestUtils.resourceToBytes("allSops/F2016C00973.pdf"))
    val cleansed = GenericCleanser.removeCompilationFootnote(rawTextFromAnxietyDisorder)
    assert(!cleansed.contains(" Statement of Principles concerning anxiety disorder No. 102 of 2014 8"))
    assert(!cleansed.contains("Compilation No. 1  Compilation date: 02/11/2016"))
    println(cleansed)
  }

  test("Chomp dictionary footnotes")
  {
    val input = Conversions.pdfToPlainText(ParserTestUtils.resourceToBytes("sops_rh/F2017L00004.pdf"))
    val preCleansed = GenericCleanser.cleanse(input)
    val result = PostAug2015Cleanser.removeFootnotes(preCleansed)
    assert(!result.contains("8 of 8"))
    println(result)
  }


  test("No sch1 dictionary headnotes")
  {
    val input = Conversions.pdfToPlainText(ParserTestUtils.resourceToBytes("sops_rh/F2017L00004.pdf"))
    val result = PostAug2015Cleanser.cleanse(input)
    assert(!result.contains("Schedule 1 - Dictionary"))
    println(result)
  }


}
