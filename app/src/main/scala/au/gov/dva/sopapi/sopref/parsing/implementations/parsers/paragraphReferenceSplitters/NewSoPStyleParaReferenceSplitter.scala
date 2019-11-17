package au.gov.dva.sopapi.sopref.parsing.implementations.parsers.paragraphReferenceSplitters

import au.gov.dva.sopapi.ConfigurationRuntimeException
import au.gov.dva.sopapi.sopref.parsing.traits.ParaReferenceSplitter


class NewSoPStyleParaReferenceSplitter extends ParaReferenceSplitter {

  val hasSubRefsRegex ="""([0-9]+\([0-9]+\))(\([a-z]+\))""".r

  override def hasSubParas(fullReference: String) : Boolean = hasSubRefsRegex.findFirstIn(fullReference).isDefined

  override def trySplitParagraphReferenceToMainParagraphAndFirstLevelSubParagraph(fullReference: String) : (String,String) = {

    if (!hasSubParas(fullReference)) throw new ConfigurationRuntimeException("SoP factor reference does not have sub paragraphs: " + fullReference)

    def splitNewStyleSopSubParaReference(wholeRef: String) = {
      // take to end of first )
      assert(hasSubParas(wholeRef))
      val m = hasSubRefsRegex.findFirstMatchIn(wholeRef).get
      (m.group(1), m.group(2))
    }

    splitNewStyleSopSubParaReference(fullReference)
  }
}




