
package org.au.dva.sopapi.textanalytics

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.time.{OffsetDateTime, ZoneId}

import au.gov.dva.sopapi.interfaces.model.{Factor, SoP, SoPPair}
import au.gov.dva.sopapi.{AppSettings, DateTimeUtils}
import au.gov.dva.sopapi.sopref.SoPs
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.sopref.text_analytics.GetKeyPhrasesClient
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.google.common.collect.{ImmutableList, ImmutableSet}
import org.asynchttpclient.DefaultAsyncHttpClient

import scala.collection.JavaConverters._



case class TaResultForSingleInstrument(frlId: String, resultsByPara: List[(String, List[String])])

object TAReport extends App {

  private def toSoP(jsonString: String) = {
    val om = new ObjectMapper()
    val jsonNode: JsonNode = om.readTree(jsonString)
    StoredSop.fromJson(jsonNode)
  }


  private def getFactorKvpsForSopPair(sopPair: SoPPair): List[(String, String)] = {

    def getFactorsAsKvps(sop: SoP) = {
      val allFactors = ImmutableList.builder().addAll(sop.getOnsetFactors).addAll(sop.getAggravationFactors).build()
      allFactors.asScala.toList.map(f => (sop.getRegisterId + "_" + f.getParagraph, f.getText))
    }

    return getFactorsAsKvps(sopPair.getRhSop) ++ getFactorsAsKvps(sopPair.getBopSop)

  }


  def getSoPForId(id: String) = id.takeWhile(c => c != '_').mkString

  def getParaForId(id: String) = id.reverse.takeWhile(c => c != '_').reverse.mkString


  def resultsToTaResults(rawResults: List[(String,List[String])]): List[TaResultForSingleInstrument] = {
    val resultsGroupedByFrlKey = rawResults.groupBy(result => getSoPForId(result._1))
    resultsGroupedByFrlKey.map(r => TaResultForSingleInstrument(r._1, r._2.map(paraPhrases => (getParaForId(paraPhrases._1),paraPhrases._2)))).toList
  }

  val dirName = "C:\\Users\\nick\\OneDrive\\Documents\\GLT\\GLTS\\GLTS Accounts\\DVA\\sops download 18 feb";

  val sourceFiles: List[File] = new java.io.File(dirName).listFiles.toList


  val allSops: List[SoP] = sourceFiles
    .flatMap(file => {
      try {
        val asString = Files.readAllLines(file.toPath).asScala.mkString(util.Properties.lineSeparator)
        val asSop = toSoP(asString)
        Some(asSop)
      }
      catch {
        case e: Throwable => {
          println(file + ": " + e.getMessage)
          None
        }
      }
    })

  val javaImmutableSet: ImmutableSet[SoP] = ImmutableSet.copyOf(allSops.asJava.iterator())

  // val repo = new AzureStorageRepository(AppSettings.AzureStorage.getConnectionString)
  val asyncHttpClient = new DefaultAsyncHttpClient()

  val sopPairs = SoPs.groupSopsToPairs(javaImmutableSet, OffsetDateTime.now(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE)))

  val taClient = new GetKeyPhrasesClient(AppSettings.AzureTextAnalyticsApi.getHost, AppSettings.AzureTextAnalyticsApi.getAPIKey, asyncHttpClient)

  val requestData: List[(String, String)] = sopPairs.asScala.flatMap(getFactorKvpsForSopPair).toList.take(10)

  val rawResults = taClient.GetKeyPhrases(requestData).toList

  // buid report
  val outputDir: Path = Files.createTempDirectory(null)
  val taResults = resultsToTaResults(rawResults)
  val report = new TextAnalyticsReport(sopPairs.asScala.toList, taResults, asyncHttpClient)


  //val outputPath = Paths.get(outputDir.toAbsolutePath.toString,fn)
  //report.writeXmlReport(outputPath)
  //Thread.sleep(60000)


}
