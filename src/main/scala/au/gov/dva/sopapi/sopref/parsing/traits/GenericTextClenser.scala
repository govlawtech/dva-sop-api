package au.gov.dva.sopapi.sopref.parsing.traits

import com.typesafe.scalalogging.Logger

import scala.util.matching.Regex


trait GenericTextClenser extends SoPClenser  {

  val logger = Logger[GenericTextClenser]

  private def removeAuthorisedFootnote(raw : String) : String = {
    val footNoteRegex1 = """(?i)Federal\s+Register\s+of\s+Legislative\s+Instruments(?-i)\s+F[0-9]{4,4}[A-Z0-9]{6,6}(?-i)""".r
    val footNoteRegex2 = """(?i)Authorised\s+Version\s+F[0-9]{4,4}[A-Z0-9]{6,6}\s+registered\s+[0-9]{2,2}/[0-9]{2,2}/[0-9]{4,4}(?-i)""".r
    List(raw).map(regexReplace(footNoteRegex1,_))
      .map(regexReplace(footNoteRegex2,_))
      .head
  }

  private def regexReplace(regex : Regex, target : String, replacement : String = "")  = {
    val matches = regex.findAllMatchIn(target);
    val matchCount = matches.size
    logger.debug("Found and removed " + matchCount + " matches of regex '" + regex.pattern.toString + "'")
    if (matchCount > 0)
      regex.replaceAllIn(target,replacement);
    else target;
  }

  private def removePageNumberFootNote(raw : String) : String = {
    val pageNumberRegex = """(?i)Page\s+[0-9]+\s+of\s+[0-9]+\s+of\s+Instrument\s+No.?\s+[0-9]+\s+of\s+[0-9]{4,4}""".r
    regexReplace(pageNumberRegex,raw)
  }

  private def trimStart(raw : String): String = {
    var startRegex = """^[^a-zA-Z0-9]*""".r
    startRegex.replaceFirstIn(raw,"")
  }

  private def removePageGaps(raw : String) : String = {
    val pageGapRegex = """(\x{0020}*[\r\n]+){2,}""".r
    regexReplace(pageGapRegex,raw,"\r\n");
  }

  private def compressSpaces(raw : String) : String = {
    val multipleSpacesRegex = """\x{0020}{2,}""".r
    regexReplace(multipleSpacesRegex,raw," ")
  }


  override def clense(rawText: String) = {
    List(rawText)
      .map(trimStart)
      .map(removeAuthorisedFootnote)
      .map(removePageNumberFootNote)
      .map(removePageGaps)
      .map(compressSpaces)
      .head
  }
}
