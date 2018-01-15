package au.gov.dva.sopapi.sopref.text_analytics.msSampleClient

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.asynchttpclient.{AsyncHttpClient, DefaultAsyncHttpClient}
import scala.collection.JavaConverters._

class GetKeyPhrasesClient(val host: String, val accessKey: String, asyncHttpClient: AsyncHttpClient) {

  private val path = "/text/analytics/v2.0/keyPhrases"

  def GetKeyPhrases(requests: Map[String, String]): Map[String, Set[String]] = {

    val body = toJsonString(buildJsonRequest(requests))

    val response = asyncHttpClient.preparePost(host + path)
      .addHeader("Content-Type", "text/json")
      .addHeader("Ocp-Apim-Subscription-Key", accessKey)
      .setBody(body)
      .execute()
      .toCompletableFuture()
      .get()

    val rawResponse = response.getResponseBody
    val asMap = deserializeResponse(rawResponse)
    asMap
  }

  private def buildJsonRequest(requestData: Map[String, String]): JsonNode = {
    val om = new ObjectMapper()
    val root = om.createObjectNode()

    val documentObjectLiterals: List[JsonNode] = requestData
      .map(r => {
        val docRequestNode = om.createObjectNode()
        docRequestNode.put("id", r._1)
        docRequestNode.put("language", "en")
        docRequestNode.put("text", r._2)
        docRequestNode
      }).toList

    val documentsArray = root.putArray("documents")
    documentObjectLiterals.foreach(d => documentsArray.add(d))
    return root


  }

  private def toJsonString(jsonNode: JsonNode) = {
    new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode)
  }

  private def deserializeResponse(responseBody: String) = {
    val objectMapper = new ObjectMapper()
    val deserialisedResponse = objectMapper.readTree(responseBody)

    val resultsForEachDoc: Map[String, Set[String]] = deserialisedResponse.findPath("documents").elements().asScala.toList
      .map(jsonNode => {
        val phrases: Set[String] = jsonNode.get("keyPhrases").elements().asScala.toList.map(_.asText()).toSet
        val id = jsonNode.get("id").asText()
        (id, phrases)
      }).toMap

    resultsForEachDoc
  }
}