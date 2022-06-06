package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

object IHDClenser extends  GenericTextClenser {
  val endNoteLine = """(?s)Endnotes.*26\s""".r


  override def clense(rawText: String): String =
  {
    val endNoteLinesInToc = endNoteLine.findFirstIn(rawText)
    var rawWithEndNoteLinesRemoved = rawText.replace(endNoteLinesInToc.get,"")

    super.clense(rawWithEndNoteLinesRemoved)

  }
}


object ContentsClenser extends GenericTextClenser {

  val endNoteLine = """(?s)Endnotes.*(?=1 Name)""".r


  override def clense(rawText: String): String =
  {
    val endNoteLinesInToc = endNoteLine.findFirstIn(rawText)
    var rawWithEndNoteLinesRemoved = rawText.replace(endNoteLinesInToc.get,"")

    super.clense(rawWithEndNoteLinesRemoved)

  }
}
