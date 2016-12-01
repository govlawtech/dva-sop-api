package au.gov.dva.sopref.parsing

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex


protected object SoPExtractors {
  def getSectionLines(clensedSopText : String, paragraphLineRegex : Regex) : (Int,List[String]) = {

    val sectionHeaderLineRegex = """^([0-9]+)\.\s""".r

    var paraLines = new ListBuffer[String]
    var passedPara = false;
    var finishedPara = false;
    var sectionHeaderLinesPassed = 0;
    val lines = clensedSopText.split("[\n\r]+")
    var sectionNumber : Option[Int] = None
    var index = 0
    while (index < lines.length && !finishedPara)
      {
        val currentLine = lines(index)
        if (!passedPara &&  !paragraphLineRegex.findFirstIn(currentLine).isEmpty)
          passedPara = true;

        if (passedPara && !sectionHeaderLineRegex.findFirstIn(currentLine).isEmpty) {
          val paraNumberMatch = sectionHeaderLineRegex.findFirstMatchIn(currentLine).get.group(1)
          if (sectionNumber.isEmpty)
            sectionNumber = Some(paraNumberMatch.toInt)
          sectionHeaderLinesPassed = sectionHeaderLinesPassed + 1
        }

        if (passedPara && !finishedPara && sectionHeaderLinesPassed == 2)
          finishedPara = true;

        if (passedPara && !finishedPara)
          paraLines += currentLine
        index = index + 1
      }

    assert(!sectionNumber.isEmpty)
    paraLines.remove(0)
    paraLines.remove(paraLines.size - 1)
    paraLines(0) = sectionHeaderLineRegex.replaceFirstIn(paraLines(0),"")
    (sectionNumber.get, paraLines.toList)

  }
}

class LsExtractor extends SoPExtractor {
  override def extractFactorSection(plainTextSop: String): String = null

  override def extractDefinitionsSection(plainTextSop: String): String = null

  override def extractCommencementSection(plainTextSop: String): String = null

  override def extractCitation(plainTextSop: String): String = null
}
