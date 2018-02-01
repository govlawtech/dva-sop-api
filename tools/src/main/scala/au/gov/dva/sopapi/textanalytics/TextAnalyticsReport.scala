
package au.gov.dva.sopapi.textanalytics

import java.lang.String
import java.nio.file.Path

import au.gov.dva.sopapi.interfaces.model.{SoP, SoPPair}
import com.google.common.base.Charsets
import com.google.common.collect.ImmutableList
import com.google.common.io.Files
import org.apache.commons.csv.{CSVFormat, CSVPrinter}

import scala.collection.JavaConverters._

class TextAnalyticsReport(sopPairs: List[SoPPair], resultsByPara: List[TaResultForSingleInstrument], asyncHttpClient: org.asynchttpclient.AsyncHttpClient) {

  val lineEndingRegex = """\r\n?|\n""".r

  def flattenSopPairsInOrder(sopPairs: List[SoPPair]): List[SoP] = {
    sopPairs.sortBy(sp => sp.getConditionName).flatMap(sp => List(sp.getRhSop, sp.getBopSop))
  }

  val orderedSoPs = flattenSopPairsInOrder(sopPairs)
  val mapOfSoPsByRegisterIdToFullTextParas = orderedSoPs.map(s => s.getRegisterId -> s).toMap

  def stripLineEndings(text: String) = lineEndingRegex.replaceAllIn(text," ")

  def getFullTextForPara(frlId: String, para: String) = {
    val allFactors = ImmutableList.builder()
      .addAll(mapOfSoPsByRegisterIdToFullTextParas(frlId).getOnsetFactors)
      .addAll(mapOfSoPsByRegisterIdToFullTextParas(frlId).getAggravationFactors)
      .build().asScala.toList
    val relevantFactor = allFactors.find(f => f.getParagraph == para)
    relevantFactor.get.getText
  }

  def createCsvReport(outputPath: Path) = {

    val mapOfResultsByFrlId = resultsByPara.map(r => r.frlId -> r.resultsByPara).toMap

    val appendable = Files.newWriter(outputPath.toFile,Charsets.UTF_8)
    val cSVPrinter = new CSVPrinter(appendable,CSVFormat.EXCEL
      .withHeader("Condition","Standard of Proof","FRL Link","Paragraph","Full Text","Key Phrases from Microsoft Text Analytics API"))

    orderedSoPs.foreach(sop => {
      mapOfResultsByFrlId(sop.getRegisterId).foreach(para => {
        val keyPhrases = para._2.mkString("; ")
        val newRecord = List(sop.getConditionName,
          sop.getStandardOfProof,
          "https://legislation.gov.au/Details/" + sop.getRegisterId,
          para._1,
          stripLineEndings(getFullTextForPara(sop.getRegisterId, para._1)),
          keyPhrases).asJava

        cSVPrinter.printRecord(newRecord)
      })
    })

    cSVPrinter.close()
    appendable.close()
    println("Wrote: " + outputPath.toAbsolutePath.toString)
  }
}
