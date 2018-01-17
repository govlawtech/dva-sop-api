import au.gov.dva.sopapi.AppSettings
import au.gov.dva.sopapi.sopref.text_analytics.GetKeyPhrasesClient
import org.asynchttpclient.DefaultAsyncHttpClient
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AzureAPITests extends FunSuite
{

  // you need to set AZURE_TEXT_ANALYTICS_ACCESS_KEY first
  test("Call Azure API")
  {
    val httpClient = new DefaultAsyncHttpClient()
    val client = new GetKeyPhrasesClient(AppSettings.AzureTextAnalyticsApi.getHost,AppSettings.AzureTextAnalyticsApi.getAPIKey,httpClient)
    val requests = List(("1", "the quick brown fox jumped over the lazy dog"),("2","humpty dumpty fell off the wall"))


    val result = client.GetKeyPhrases(requests)
    println(result)

  }

}
