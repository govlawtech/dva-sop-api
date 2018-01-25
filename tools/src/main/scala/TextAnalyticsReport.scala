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

class TextAnalyticsReport(sopPairs: List[SoPPair], results: List[(String,List[String])], asyncHttpClient: org.asynchttpclient.AsyncHttpClient) {


  def getSoPForId(id: String) = id.takeWhile(c => c != '_').mkString

  def getParaForId(id: String) = id.reverse.takeWhile(c => c != '_').reverse.mkString

  def getResultsForSop(sop : SoP) = {

  }




  def writeCsvReport(outputPath : Path) = {

    val sb = new StringBuilder
    sb.append("Condition, FRL Link, Paragraph, Full Text, Key Phrases from Microsoft Text Analytics API")



    Files.write(outputXml.toString(), outputPath.toFile , Charsets.UTF_8)

    println("Wrote: " + outputPath.toAbsolutePath.toString)

  }






}
