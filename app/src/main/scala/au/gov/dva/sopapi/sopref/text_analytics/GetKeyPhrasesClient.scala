package au.gov.dva.sopapi.sopref.text_analytics

import au.gov.dva.sopapi.exceptions.DvaSopApiRuntimeException
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.asynchttpclient.{AsyncHttpClient, DefaultAsyncHttpClient}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class GetKeyPhrasesClient(val host: String, val accessKey: String, asyncHttpClient: AsyncHttpClient) {

  private val path = "/text/analytics/v2.0/keyPhrases"

  def GetKeyPhrases(requests: List[(String, String)]): List[(String, List[String])] = {

    val longDocs = requests.filter(r => r._2.length > 5000)
    if (!longDocs.isEmpty) throw new IllegalArgumentException("Docs too long: " + longDocs.mkString(util.Properties.lineSeparator))

    val batched = requests.sliding(300, 300).toList

    val jsonRequests = batched.map(batch => toJsonString(buildJsonRequest(batch)))

    val maxRequestSize = jsonRequests.map(_.size).max

    if (maxRequestSize > 1000000) throw new IllegalArgumentException("Max request size is 1MB, actual is " + maxRequestSize + " bytes.")

    var acc = new ListBuffer[(String,List[String])]()

    val results = batched.foreach(batch => {

      val body = toJsonString(buildJsonRequest(requests))

      val response = asyncHttpClient.preparePost(host + path)
        .addHeader("Content-Type", "text/json; charset=utf-8")
        .addHeader("Ocp-Apim-Subscription-Key", accessKey)
        .setBody(body)
        .execute()
        .toCompletableFuture()
        .get()
      Thread.sleep(100)
      if (response.getStatusCode != 200) {
        throw new DvaSopApiRuntimeException(s"Text Analytics API returned failed response for REQUEST: ${util.Properties.lineSeparator} $body: ${util.Properties.lineSeparator} RESPONSE: $response" )
      }

      else {
        val rawResponse = response.getResponseBody
        val asMap: List[(String, List[String])] = deserializeResponse(rawResponse)
        acc.appendAll(asMap)
      }
    })

    acc.toList

  }

  private def buildJsonRequest(requestData: List[(String, String)]): JsonNode = {
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

  private def deserializeResponse(responseBody: String): List[(String, List[String])] = {
    val objectMapper = new ObjectMapper()
    val deserialisedResponse = objectMapper.readTree(responseBody)

    val errors = deserialisedResponse.findPath("errors").elements().asScala.toList
      .map(error => (error.get("id").asText(), error.get("message").asText()))

    if (!errors.isEmpty) {
      println("Errors: " + errors)
      return List()
    }

    val resultsForEachDoc: List[(String, List[String])] = deserialisedResponse.findPath("documents").elements().asScala.toList
      .map(jsonNode => {
        val phrases = jsonNode.get("keyPhrases").elements().asScala.toList.map(_.asText())
        val id = jsonNode.get("id").asText()
        (id, phrases)
      })

    return resultsForEachDoc
  }

}