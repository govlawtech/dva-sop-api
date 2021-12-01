package au.gov.dva.sopapi.sopref.parsing.implementations.extractors


class IschaemicHeartDiseaseSyndromeExtractor(clensedText : String) extends  PostAug2015Extractor(clensedText)  {
  override def extractDateOfEffectSection(plainTextSop: String): String = "This instrument commences on 25 January 2016."
}



