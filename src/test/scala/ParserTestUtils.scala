
package au.gov.dva.sopapi.tests.parsers;

import java.io.InputStream
import java.nio.charset.Charset

import au.gov.dva.sopapi.sopref.data.Conversions
import au.gov.dva.sopapi.sopref.parsing.ServiceLocator
import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers.GenericCleanser
import com.google.common.base.Charsets
import com.google.common.io.Resources

import scala.io.Source

object ParserTestUtils {
  def resourceToBytes(resourcePath : String) = {
    val sourceUrl = Resources.getResource(resourcePath);
    Resources.toByteArray(sourceUrl)
  }

  def resourceToString(resourcePath : String) = {

    val sourceUrl = Resources.getResource(resourcePath);
    Resources.toString(sourceUrl,Charsets.UTF_8)
  }

  def executeWholeParsingPipeline(registerId : String, resourcePath : String) = {
      val bytes = resourceToBytes(resourcePath);
      val rawText = Conversions.pdfToPlainText(bytes);
      val cleansedText = ServiceLocator.findTextCleanser(registerId).cleanse(rawText)
      val sopFactory = ServiceLocator.findSoPFactory(registerId)
      val sop = sopFactory.create(registerId, cleansedText)
      sop
  }

  def produceCleansedText(registerId: String, resourcePath : String) = {
    val bytes = resourceToBytes(resourcePath);
    val rawText = Conversions.pdfToPlainText(bytes);
    val cleansedText = ServiceLocator.findTextCleanser(registerId).cleanse(rawText)
    cleansedText
  }


}
