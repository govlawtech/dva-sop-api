package org.au.dva.sopapi.textanalytics

import au.gov.dva.sopapi.interfaces.model.SoP

/**
  * Created by nick on 1/25/2018.
  */
trait KeyPhraseFilter {

  val globalBlackList : List[String => Boolean] = List(
    _.contains("clinical"),
    _.contains("inability")



  )

  def shouldRemove(phrase : String, sop : SoP) = {

  }
}
