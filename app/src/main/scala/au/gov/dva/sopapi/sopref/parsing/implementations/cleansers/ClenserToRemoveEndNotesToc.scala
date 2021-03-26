package au.gov.dva.sopapi.sopref.parsing.implementations.cleansers

import au.gov.dva.sopapi.sopref.parsing.traits.GenericTextClenser

object ClenserToRemoveEndNotesToc extends GenericTextClenser {

  override def clense(rawText: String): String = {
    // remove endnotes from Toc
    val endNotesRegex = """Endnotes ....""".r
    val withFirstEndNotesLineRemoved = endNotesRegex.replaceFirstIn(rawText, "")
    super.clense(withFirstEndNotesLineRemoved)

  }
}

