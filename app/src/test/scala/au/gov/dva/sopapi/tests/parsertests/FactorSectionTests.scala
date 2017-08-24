package au.gov.dva.sopapi.tests.parsertests

import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.{FactorsParser, PreAugust2015Parser}
import au.gov.dva.sopapi.sopref.parsing.traits.MiscRegexes
import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.immutable
import scala.util.Properties


@RunWith(classOf[JUnitRunner])
class FactorSectionTests extends FunSuite with MiscRegexes {

  test("Split factors sections to individual factors for ls") {
    val input = ParserTestUtils.resourceToString("factorSections/lsFactorLines.txt").split(scala.util.Properties.lineSeparator).toList
    val excludingHead = input.drop(3)
    val result = SoPExtractorUtilities.splitFactorsSectionByFactor(excludingHead)
    assert(result.size == 31)
    println(result)
  }

  test("Split factor section to individual factors for osteo") {
    val input = ParserTestUtils.resourceToString("factorSections/osteoFactorLines.txt").split(scala.util.Properties.lineSeparator).toList
    val excludingHead = input.drop(4)
    val result = SoPExtractorUtilities.splitFactorsSectionByFactor(excludingHead)
    println(result)
    assert(result.size == 40 && result.forall(l => l.head.startsWith("(")))

  }

  test("Split factors for sleep apnoae") {
    // this one is tricky because the (h) para has sub paras starting with (i) and the next main para also starts with (i)
    val input = ParserTestUtils.resourceToString("factorSections/sleepApnoeaFactorLines.txt").split(scala.util.Properties.lineSeparator).toList
    val excludingHeader = input.drop(3)
    val result = SoPExtractorUtilities.splitFactorsSectionByFactor(excludingHeader)
    println(result)
    val numberOfFactorsStartingWithI = result.filter(i => i(0).startsWith("(i)")).size
    assert(numberOfFactorsStartingWithI == 1)
  }


  test("Split factors section to head and rest") {
    val input = ParserTestUtils.resourceToString("factorSections/sleepApnoeaFactorLines.txt").split(scala.util.Properties.lineSeparator).toList
    val (header, rest) = SoPExtractorUtilities.splitFactorsSectionToHeaderAndRest(input)
    println("HEADER: " + header)
    println("REST: " + rest.mkString(Properties.lineSeparator))
    assert(!header.isEmpty && !rest.isEmpty)
  }


  test("Parse factors section for carpal tunnel")
  {

    val input = ParserTestUtils.resourceToString("carpalTunnelFactorsText.txt");
    try {
      val result = PreAugust2015Parser.parseFactorsSection(input)
    }
    catch
      {
        case e : Throwable => println(e)
      }
  }

  test("Parse factors section for anxiety disorder with fallback parser")
  {
    val factorsText = ParserTestUtils.resourceToString("factors/anxietyDisorderFactors.txt")
    val lines = factorsText.split(platformNeutralLineEndingRegex.regex).toList

    val mainParas = FactorsParser.oldStyleSmallLetterLinesToFactors(lines,(_,_) => false)
    mainParas.foreach(m => println(m.getLetter))
  }

  test("Parse osteo factors with fallback parser")
  {
    val factorsText = ParserTestUtils.resourceToString("factors/osteoFactorLinesWithoutHead.txt")
    val lines = factorsText.split(platformNeutralLineEndingRegex.regex).toList

    val mainParas = FactorsParser.oldStyleSmallLetterLinesToFactors(lines,(_,_) => false)
    mainParas.foreach(m => println(m.getLetter))
    assert(mainParas.size == 40 )
  }

}
