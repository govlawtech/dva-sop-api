
package au.gov.dva.sopapi.textanalytics

import java.io.File
import java.nio.file.Files

import au.gov.dva.sopapi.interfaces.model.SoP
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

import scala.collection.JavaConverters._

object SoPData {

  private def toSoP(jsonString: String) = {
    val om = new ObjectMapper()
    val jsonNode: JsonNode = om.readTree(jsonString)
    StoredSop.fromJson(jsonNode)
  }

  def loadFromFileSystem(dirName : String ) = {
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

    allSops
  }



}
