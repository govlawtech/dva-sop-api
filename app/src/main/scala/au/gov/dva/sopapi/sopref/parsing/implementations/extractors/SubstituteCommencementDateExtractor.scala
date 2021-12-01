package au.gov.dva.sopapi.sopref.parsing.implementations.extractors

class SubstituteCommencementDateExtractor(clensedText : String, longFormDate: String) extends PostAug2015Extractor(clensedText) {

  override def extractDateOfEffectSection(plainTextSop: String): String = s"This instrument commences on $longFormDate."

}
