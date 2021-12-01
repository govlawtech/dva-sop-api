package au.gov.dva.sopapi.sopref.parsing.implementations.extractors


class NhLExtractor(clensedText : String) extends  PostAug2015Extractor(clensedText)  {
  override def extractDateOfEffectSection(plainTextSop: String): String = "This instrument commences on 26 November 2018."
}


