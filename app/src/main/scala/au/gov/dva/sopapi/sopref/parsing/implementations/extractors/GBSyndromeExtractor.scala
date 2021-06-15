package au.gov.dva.sopapi.sopref.parsing.implementations.extractors

class GBSyndromeExtractor(clensedText : String) extends  PostAug2015Extractor(clensedText)  {
    override def extractDateOfEffectSection(plainTextSop: String): String = "This instrument commences on 2 April 2018."
}



