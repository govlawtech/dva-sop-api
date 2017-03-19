

package au.gov.dva.sopapi.tests.parsers;

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.sopref.data.Conversions
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities._
import au.gov.dva.sopapi.sopref.parsing.implementations._
import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers.GenericCleanser
import au.gov.dva.sopapi.sopref.parsing.implementations.extractors.PreAugust2015Extractor
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.{PreAug2015DefinitionsParsers, PostAugust2015Parser, PreAugust2015Parser}
import au.gov.dva.sopapi.sopref.parsing.implementations.sopfactories.LsSoPFactory
import au.gov.dva.sopapi.sopref.parsing.traits.FactorsParser
import com.google.common.io.Resources
import org.scalatest.{FlatSpec, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.io.Source
import scala.util.Properties


@RunWith(classOf[JUnitRunner])
class PostAug2015ParserTests extends FunSuite{

  ignore("Parse definitions section")
  {
    val defsSection = ParserTestUtils.resourceToString("definitions/post2015HemsDefs.txt");
    val underTest = PostAugust2015Parser
    val result = underTest.parseDefinitions(defsSection)
    assert(result.size == 7 )


  }
}
