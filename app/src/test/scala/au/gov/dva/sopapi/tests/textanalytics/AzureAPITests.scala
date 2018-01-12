import au.gov.dva.sopapi.AppSettings
import au.gov.dva.sopapi.sopref.text_analytics.msSampleClient._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AzureAPITests extends FunSuite
{

  // you need to set AZURE_TEXT_ANALYTICS_ACCESS_KEY first
  test("Call Azure API")
  {
    val client = new GetKeyPhrases(AppSettings.AzureTextAnalyticsApi.getHost,AppSettings.AzureTextAnalyticsApi.getAPIKey)
    val documents = new Documents()
    documents.add("1","en","the quick brown fox jumped over the lazy dog")
    val result = client.GetKeyPhrases(documents)
    println(result)

  }

}
