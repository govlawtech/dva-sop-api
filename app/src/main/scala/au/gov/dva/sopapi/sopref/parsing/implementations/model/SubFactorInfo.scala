package au.gov.dva.sopapi.sopref.parsing.implementations.model

case class SubFactorInfo(para : String, text : String)
{
  def stripTrailingPunctuation: SubFactorInfo = {
    return new SubFactorInfo(para,text.replaceFirst("((; or)|(;))$",""))
  }
}