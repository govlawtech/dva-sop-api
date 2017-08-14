

package au.gov.dva.sopapi.tests.parsers;

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.sopref.data.Conversions
import au.gov.dva.sopapi.sopref.parsing.{RegisterIdInfo, SoPExtractorUtilities}
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities._
import au.gov.dva.sopapi.sopref.parsing.implementations._
import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers.GenericClenser
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.{PostAug2015FactorsParser, PostAugust2015Parser, PreAug2015DefinitionsParsers, PreAugust2015Parser}
import au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories.LsSoPFactory
import au.gov.dva.sopapi.sopref.parsing.traits.PreAug2015FactorsParser
import com.google.common.io.Resources
import org.scalatest.{FlatSpec, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.io.Source
import scala.util.Properties


@RunWith(classOf[JUnitRunner])
class PostAug2015ParserTests extends FunSuite{

  test("Parse definitions section")
  {
    val defsSection = ParserTestUtils.resourceToString("definitions/post2015HemsDefs.txt");
    val underTest = PostAugust2015Parser
    val result = underTest.parseDefinitions(defsSection)

    result.foreach(i => System.out.println(i.getTerm + ": " + i.getDefinition + Properties.lineSeparator))
    assert(result.size == 11)
  }

  test("Parse 2017 haemorroids factors text")
  {
     val section = ParserTestUtils.resourceToString("factorSections/2017haemorroidsFactorsSection.txt")
     val result = PostAugust2015Parser.parseFactors(section);
  }

  test("Split 2017 haemorroids factors section to head and rest")
  {
    val section = ParserTestUtils.resourceToString("factorSections/2017haemorroidsFactorsSection.txt")
    val(head,rest) = PostAug2015FactorsParser.splitFactorListToHeadAndRest(section.split(Properties.lineSeparator).toList)
    println(head)
    println(rest)
    assert(head.last.endsWith("relevant service:") && rest.last.endsWith("haemorrhoids."));
  }

  test("Split factors list to main factors") {
    val section = ParserTestUtils.resourceToString("factorSections/2017haemorroidsFactorsSectionWithoutHead.txt")
    val factors = PostAug2015FactorsParser.splitFactorListToIndividualFactors(section.split(Properties.lineSeparator).toList)
    println(factors)
    assert(factors.size == 19 && factors.last.last.endsWith("haemorrhoids."))
  }

  test("Parse all 2017 haemorroids factors") {
    val section = ParserTestUtils.resourceToString("factorSections/2017haemorroidsFactorsSectionWithoutHead.txt")
    val factors = PostAug2015FactorsParser.splitFactorListToIndividualFactors(section.split(Properties.lineSeparator).toList)
    val parseResults = factors.map(f => PostAug2015FactorsParser.parseFactor(f))
    println(parseResults)
  }

  test("Unpack register id") {
    val input = "F2016L00001"
    val result = SoPExtractorUtilities.unpackRegisterId(input) match  {
      case RegisterIdInfo(2016,false,1) => true
      case _ =>  false

    }
    assert(result == true)
  }

}
