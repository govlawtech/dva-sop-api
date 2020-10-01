package au.gov.dva.sopapi.sopref.parsing.implementations.extractors

import au.gov.dva.sopapi.sopref.parsing.traits.OldSoPStyleExtractor

class TrochanticBursitisExtractor(clensedText : String) extends OldSoPStyleExtractor(clensedText) {
  override def extractDateOfEffectSection(plainTextSop: String): String = "This Instrument takes effect from 27 January 2015"
}

