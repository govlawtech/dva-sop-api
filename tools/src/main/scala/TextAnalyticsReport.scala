import java.io.File
import java.nio.file.Path

import au.gov.dva.sopapi.AppSettings
import au.gov.dva.sopapi.interfaces.model.{SoP, SoPPair}
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository
import au.gov.dva.sopapi.sopref.text_analytics.GetKeyPhrasesClient
import com.google.common.base.Charsets
import com.google.common.io.Files
import org.apache.commons.csv.{CSVFormat, CSVPrinter, CSVRecord}
import org.asynchttpclient.DefaultAsyncHttpClient

import scala.collection.JavaConverters._

class TextAnalyticsReport(sops: List[SoP], asyncHttpClient: org.asynchttpclient.AsyncHttpClient) {


  val taClient = new GetKeyPhrasesClient(AppSettings.AzureTextAnalyticsApi.getHost, AppSettings.AzureTextAnalyticsApi.getAPIKey, asyncHttpClient)


  def writeXmlReport(outputPath : Path) = {

    val allNodes = sops.map(sop => {

      val request = buildRequestDataForSop(sop)
      try {
        val result = taClient.GetKeyPhrases(request)
        println()
        val xmlResult = createXmlNodeForDocumentResult(sop, result)
        println("Got results for: " + sop.getRegisterId)
        xmlResult

      }
      catch {
        case _: Throwable => println("Failed: " + sop.getRegisterId)
      }
    })


    val outputXml =
      <textAnalyticsResults>
        {allNodes}
      </textAnalyticsResults>


    Files.write(outputXml.toString(), outputPath.toFile , Charsets.UTF_8)

    println("Wrote: " + outputPath.toAbsolutePath.toString)

  }

  private def buildRequestDataForSop(sop: SoP): List[(String, String)] = {
    val data: List[(String, String)] = sop.getOnsetFactors.asScala.toList
      .map(f => (f.getParagraph, f.getText))

    data
  }


  private def createRecordsForResult(sop: SoP, result: List[(String, List[String])]): List[List[String]] = {
    val fullTextMap = sop.getOnsetFactors.asScala.toList
      .map(f => (f.getParagraph -> f.getText))
      .toMap

    val resultMap: Map[String, List[String]] = result.toMap

    val csvLines = fullTextMap.map(para => List(
      sop.getConditionName,
      para._1,
      para._2,
      resultMap(para._1).mkString(";")
    )).toList

    csvLines

  }


  private def createXmlNodeForDocumentResult(sop: SoP, result: List[(String, List[String])]) = {

    val fullTextMap = sop.getOnsetFactors.asScala.toList
      .map(f => (f.getParagraph -> f.getText))
      .toMap

    val nodeForSop =
      <sop frlId={sop.getRegisterId} conditionName={sop.getConditionName} standardOfProof={sop.getStandardOfProof.toString}>
        <factors>
          {result.toList.sortBy(i => i._1).map(result => {
          <factor paragraph={result._1}>
            <fullText>
              {fullTextMap(result._1)}
            </fullText>
            <keyPhrases>
              {result._2.map(kp => <phrase>
              {kp}
            </phrase>)}
            </keyPhrases>
          </factor>
        })}
        </factors>

      </sop>


    nodeForSop

  }




  def createCsvReport(sops: List[SoP], target: Appendable) = {


    val csvPrinter = new CSVPrinter(target, CSVFormat.EXCEL)


    val allNodes = sops.map(sop => {

      val request = buildRequestDataForSop(sop)
      try {
        val result = taClient.GetKeyPhrases(request)
        val xmlResult = createXmlNodeForDocumentResult(sop, result)
        println("Got results for: " + sop.getRegisterId)
        xmlResult

      }
      catch {
        case _: Throwable => println("Failed: " + sop.getRegisterId)
      }
    })


    val outputXml =
      <textAnalyticsResults>
        {allNodes}
      </textAnalyticsResults>


    val tempDir: File = Files.createTempDir()
    val outputFile = tempDir + "/taResults.xml"
    Files.write(outputXml.toString(), new File(outputFile), Charsets.UTF_8)

    println("Wrote: " + outputFile)
    println("Have a nice day.")
  }


}
