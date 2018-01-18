import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.time.{OffsetDateTime, ZoneId}

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.{AppSettings, DateTimeUtils}
import au.gov.dva.sopapi.sopref.SoPs
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.google.common.collect.{ImmutableList, ImmutableSet}
import org.asynchttpclient.DefaultAsyncHttpClient

import scala.collection.JavaConverters._

/**
  * Created by nick on 1/18/2018.
  */
object TAReport extends App {

  private def toSoP(jsonString: String) = {
    val om = new ObjectMapper()
    val jsonNode: JsonNode = om.readTree(jsonString)
    StoredSop.fromJson(jsonNode)
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



   val javaImmutableSet : ImmutableSet[SoP] = ImmutableSet.copyOf(allSops.asJava.iterator())

  // val repo = new AzureStorageRepository(AppSettings.AzureStorage.getConnectionString)
  val asyncHttpClient = new DefaultAsyncHttpClient()

  val sopPairs = SoPs.groupSopsToPairs(javaImmutableSet, OffsetDateTime.now(ZoneId.of(DateTimeUtils.TZDB_REGION_CODE)))

  val sops = sopPairs.asScala.flatMap(sp => List(sp.getRhSop, sp.getBopSop)).toList

  val batched: Iterator[(List[SoP], Int)] =  sops.sliding(50,50) zipWithIndex

  val outputDir: Path = Files.createTempDirectory(null)

//  println(sops.map(s => s.getRegisterId).sorted.mkString(util.Properties.lineSeparator))
  batched.foreach(batch => {

    val fileName =  "batch " + batch._2 + ".xml"
    val report = new TextAnalyticsReport(batch._1, asyncHttpClient)

    val fn = "batch " + batch._2 + ".xml"
    val outputPath = Paths.get(outputDir.toAbsolutePath.toString,fn)
    report.writeXmlReport(outputPath)
    Thread.sleep(60000)


  })




}
