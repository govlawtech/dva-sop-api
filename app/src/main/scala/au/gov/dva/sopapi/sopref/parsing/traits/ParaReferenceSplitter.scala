package au.gov.dva.sopapi.sopref.parsing.traits

trait ParaReferenceSplitter {
  def hasSubParas(fullReference: String) : Boolean
  def trySplitParagraphReferenceToMainParagraphAndFirstLevelSubParagraph(fullReference: String) : (String,String)
}
