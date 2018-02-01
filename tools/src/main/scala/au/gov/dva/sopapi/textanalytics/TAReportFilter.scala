package au.gov.dva.sopapi.textanalytics

import java.io.{File, FileReader}
import java.nio.file.{Files, Path, Paths}

import com.google.common.base.Charsets
import org.apache.commons.csv.{CSVFormat, CSVPrinter, CSVRecord}

import scala.collection.JavaConverters._

object TAReportFilter extends App {

  val inputCsv = args(0)
  val sopsDir = args(1)

  val sopsMap = SoPData.loadFromFileSystem(sopsDir)
      .map(s => (s.getRegisterId -> s))
    .toMap

  private def getListOfPhrases(csvRecord : CSVRecord) = {
    csvRecord.get(5).split(";").map(i => i.trim).toList
  }


  private def getKeyPhraseFilterForRecord(csvRecord : CSVRecord) : KeyPhraseFilter = {
    // for now, just use global filter
    return new SoPSpecificKeyPhraseFilter
  }

  val frlLinkRegex = """/[A-Z0-9]+$""".r
  private def printFilteredItem(originalCsvRecord: CSVRecord, csvPrinter: CSVPrinter) = {

    val phrases: List[String] = getListOfPhrases(originalCsvRecord)
    val kpFilter = getKeyPhraseFilterForRecord(originalCsvRecord)

    val sopId = TAReport.getSoPForId(frlLinkRegex.findFirstMatchIn(originalCsvRecord.get(2)).get.matched.drop(1))
    val sop = sopsMap(sopId)

    val filteredPhrases = phrases.filter(p => !kpFilter.shouldRemove(p,sop))
    val newRecord: List[String] = originalCsvRecord.iterator().asScala.toList :+ filteredPhrases.mkString("; ")
    csvPrinter.printRecord(newRecord.asJava)
  }

  val in = new FileReader(inputCsv)
  val records = CSVFormat.EXCEL.parse(in).getRecords.asScala.toList

  val outputDir: Path = Files.createTempDirectory(null)
  val outputPath = Paths.get(outputDir.toAbsolutePath.toString,"taReport.csv")
  val appendable = com.google.common.io.Files.newWriter(outputPath.toFile,Charsets.UTF_8)
  val cSVPrinter = new CSVPrinter(appendable,CSVFormat.EXCEL
    .withHeader("Condition","Standard of Proof","FRL Link","Paragraph","Full Text","Key Phrases from Direct Microsoft Text Analytics API","With Filtering Heuristics"))

  records.drop(1).foreach(record => printFilteredItem(record,cSVPrinter))

  cSVPrinter.close()
  in.close()
  appendable.close()
  println("Wrote: " + outputPath)

  println("Have a nice day")
}
