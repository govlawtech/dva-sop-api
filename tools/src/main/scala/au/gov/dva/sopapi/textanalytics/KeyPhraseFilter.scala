package au.gov.dva.sopapi.textanalytics

import au.gov.dva.sopapi.interfaces.model.SoP

/**
  * Created by nick on 1/25/2018.
  */
trait KeyPhraseFilter {

  protected val globallyBlackListedWords = List(
    "clinical",
    "inability",
    "year",
    "days",
    "week",
    "affected",
    "hours",
    "time",
    "month",
    "duration",
    "onset"

  )


  def shouldRemove(phrase : String, sop: SoP) = {
    val blackListed = !globallyBlackListedWords.filter(blackListedWord => phrase.contains(blackListedWord)).isEmpty
    if (blackListed) println(phrase)
    blackListed
  }
}



class SoPSpecificKeyPhraseFilter extends KeyPhraseFilter  {

  def containsConditionName(phrase: String, sop : SoP) = sop.getConditionName.toLowerCase.contains(phrase.toLowerCase())

  override def shouldRemove(phrase: String, sop: SoP): Boolean = {

    super.shouldRemove(phrase, sop) || containsConditionName(phrase,sop)
  }

}