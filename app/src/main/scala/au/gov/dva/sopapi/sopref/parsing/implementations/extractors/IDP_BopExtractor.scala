package au.gov.dva.sopapi.sopref.parsing.implementations.extractors

class IDP_BopExtractor(clensedText : String) extends PostAug2015Extractor(clensedText) {

  override def extractDateOfEffectSection(plainTextSop: String): String = "This instrument commences on 23 May 2016"
}





