package au.gov.dva.sopapi.sopref.parsing.implementations.extractors

import au.gov.dva.sopapi.sopref.parsing.traits.OldSoPStyleExtractor

class BlephartisExtractor(clensedText : String) extends OldSoPStyleExtractor(clensedText) {
  override def extractDateOfEffectSection(plainTextSop: String): String = "This Instrument takes effect from 1 September 2010"
}
