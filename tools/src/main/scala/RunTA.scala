import java.io.File

import au.gov.dva.sopapi.AppSettings
import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository
import au.gov.dva.sopapi.sopref.text_analytics.GetKeyPhrasesClient
import com.google.common.base.{Charsets, Utf8}
import com.google.common.io.Files
import org.asynchttpclient.DefaultAsyncHttpClient

import scala.xml
import collection.JavaConverters._

val repo = new AzureStorageRepository(AppSettings.AzureStorage.getConnectionString)
val asyncHttpClient = new DefaultAsyncHttpClient()
val taClient = new GetKeyPhrasesClient(AppSettings.AzureTextAnalyticsApi.getHost, AppSettings.AzureTextAnalyticsApi.getAPIKey, asyncHttpClient)

val sops = List(repo.getSop("F2017C00851").get())

// for each sop, make id for each factor, add every factor as a document, send request, serialise results

def buildRequestDataForSop(sop: SoP): Map[String, String] = {
  val data: Map[String, String] = sop.getOnsetFactors.asScala.toList
    .map(f => f.getParagraph -> f.getText)
    .toMap
  data
}

def createXmlNodeForDocumentResult(sop: SoP, result: Map[String, Set[String]]) = {

  val fullTextMap = sop.getOnsetFactors.asScala.toList
    .map(f => (f.getParagraph -> f.getText))
    .toMap

  val nodeForSop =
    <sop frlId={sop.getRegisterId} conditionName={sop.getConditionName}>
      <factors>
        {result.toList.sortBy(i => i._1).map(result => {
        <factor paragraph={result._1}>
          <fullText>
            {fullTextMap(result._1)}
          </fullText>
          <keyPhrases>
            {result._2.map(kp => <phrase>{kp}</phrase>)}
          </keyPhrases>
        </factor>
      })}
      </factors>

    </sop>


  nodeForSop

}

def run(sops : List[SoP]) = {

   val allNodes = sops.map(sop => {

     val request = buildRequestDataForSop(sop)
     try {
       val result = taClient.GetKeyPhrases(request)
       val xmlResult = createXmlNodeForDocumentResult(sop,result)
       println("Got results for: " + sop.getRegisterId)
       xmlResult

     }
     catch  {
       case _ : Throwable => println("Failed: " + sop.getRegisterId)
     }
   })


  val outputXml =
   <textAnalyticsResults>
      {allNodes}
  </textAnalyticsResults>


  val tempDir: File = Files.createTempDir()
  val outputFile = tempDir + "/taResults.xml"
  Files.write(outputXml.toString(),new File(outputFile),Charsets.UTF_8)

  println("Wrote: " + outputFile)
  println("Have a nice day.")
}


run(sops)
