
package org.au.dva.sopapi.textanalytics

import java.io.File
import java.nio.file.Path

import au.gov.dva.sopapi.AppSettings
import au.gov.dva.sopapi.interfaces.model.{SoP, SoPPair}
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository
import au.gov.dva.sopapi.sopref.text_analytics.GetKeyPhrasesClient
import com.google.common.base.Charsets
import com.google.common.collect.ImmutableList
import com.google.common.io.Files
import org.apache.commons.csv.{CSVFormat, CSVPrinter, CSVRecord}
import org.asynchttpclient.DefaultAsyncHttpClient

import scala.collection.JavaConverters._

class TextAnalyticsReport(sopPairs: List[SoPPair], resultsByPara : List[TaResultForSingleInstrument], asyncHttpClient: org.asynchttpclient.AsyncHttpClient) {



 //   .map(r => {
 //   val key = r._1
  ///  val frlId = getSoPForId(key)
   // val para = getParaForId(key)
   // val phrases = r._2


  //})



  def writeCsvReport(outputPath : Path) = {

    val sb = new StringBuilder
    sb.append("Condition, Standard of Proof, FRL Link, Paragraph, Full Text, Key Phrases from Microsoft Text Analytics API")

    // for each sop pair

  //  Files.write(outputXml.toString(), outputPath.toFile , Charsets.UTF_8)

    println("Wrote: " + outputPath.toAbsolutePath.toString)

  }






}
