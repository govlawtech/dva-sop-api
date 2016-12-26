
package au.gov.dva.sopapi.tests.parsers;

import java.io.InputStream
import java.nio.charset.Charset

import au.gov.dva.sopapi.sopref.data.Conversions
import au.gov.dva.sopapi.sopref.parsing.factories.SoPFactoryLocator
import au.gov.dva.sopapi.sopref.parsing.implementations.GenericClenser
import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser
import com.google.common.base.Charsets
import com.google.common.io.Resources

import scala.io.Source

object ParserTestUtils {
  def resourceToBytes(resourcePath : String) = {
    val sourceUrl = Resources.getResource(resourcePath);

    val sourceResourceStream: InputStream = Resources.asByteSource(sourceUrl).openStream();
    val bytes =  Stream.continually(sourceResourceStream.read).takeWhile(_ != -1).map(_.toByte).toArray;
    sourceResourceStream.close()
    bytes

  }

  def resourceToString(resourcePath : String) = {

    val sourceUrl = Resources.getResource(resourcePath);
    Resources.toString(sourceUrl,Charsets.UTF_8)
  }

  def executeWholeParsingPipeline(registerId : String, resourcePath : String) = {
    val bytes = resourceToBytes(resourcePath);
    val rawText = Conversions.pdfToPlainText(bytes);
    val genericClenser = new GenericClenser();
    val clensedText = genericClenser.clense(rawText)
    val sopFactory = SoPFactoryLocator.findFactory(registerId)
    val sop = sopFactory.create(registerId,clensedText)
    sop
  }


}
