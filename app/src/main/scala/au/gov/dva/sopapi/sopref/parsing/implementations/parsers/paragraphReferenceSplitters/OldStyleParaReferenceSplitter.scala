package au.gov.dva.sopapi.sopref.parsing.implementations.parsers.paragraphReferenceSplitters

import au.gov.dva.sopapi.ConfigurationRuntimeException
import au.gov.dva.sopapi.sopref.parsing.traits.ParaReferenceSplitter


class OldSoPStyleParaReferenceSplitter extends ParaReferenceSplitter {

  val hasSubsRefsRegex = """([0-9]+\([a-z]+\))(\([a-z]+\)""".r

  override def hasSubParas(fullReference: String): Boolean = hasSubsRefsRegex.findFirstIn(fullReference).isDefined

  override def trySplitParagraphReferenceToMainParagraphAndFirstLevelSubParagraph(fullReference: String): (String, String) = {

    if (!hasSubParas(fullReference)) throw new ConfigurationRuntimeException("SoP factor reference does not have sub paragraphs: " + fullReference)

    def splitNewStyleSopSubParaReference(wholeRef: String) = {
      // take to end of first )
      assert(hasSubParas(wholeRef))
      val m = hasSubsRefsRegex.findFirstMatchIn(wholeRef).get
      (m.group(1), m.group(2))
    }

    splitNewStyleSopSubParaReference(fullReference)
  }
}



