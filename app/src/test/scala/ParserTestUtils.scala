
package au.gov.dva.sopapi.tests.parsers;

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.charset.Charset

import au.gov.dva.sopapi.sopref.data.Conversions
import au.gov.dva.sopapi.sopref.parsing.ServiceLocator
import au.gov.dva.sopapi.sopref.parsing.implementations.cleansers.GenericClenser
import com.google.common.base.Charsets
import com.google.common.io.Resources

import scala.io.Source
import java.nio.file.{Files, Path}
import java.util.zip.{ZipFile, ZipInputStream}

import au.gov.dva.dvasopapi.tests.{ResourceDirectoryLoader, TestUtils}
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import com.fasterxml.jackson.databind.ObjectMapper

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.util.Try

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
      val rawText = Conversions.croppedPdfToPlaintext(bytes,registerId);
      val cleansedText = ServiceLocator.findTextCleanser(registerId).clense(rawText)
      val sopFactory = ServiceLocator.findSoPFactory(registerId)
      val sop = sopFactory.create(registerId, cleansedText)
      sop
  }

  def produceCleansedText(registerId: String, resourcePath : String) = {
    val bytes = resourceToBytes(resourcePath);
    val rawText = Conversions.croppedPdfToPlaintext(bytes,registerId);
    val cleansedText = ServiceLocator.findTextCleanser(registerId).clense(rawText)
    cleansedText
  }


  def getAllSopsInDir(resourcesDir : String) = {
    val files = new ResourceDirectoryLoader().getResourceFiles(resourcesDir).asScala
    val objectMapper = new ObjectMapper
    files.map(f => StoredSop.fromJson(objectMapper.readTree( resourceToString(s"$resourcesDir/$f"))))
  }



}
