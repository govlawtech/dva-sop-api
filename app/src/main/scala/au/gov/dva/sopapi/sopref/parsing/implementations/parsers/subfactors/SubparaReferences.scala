package au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors

import au.gov.dva.sopapi.sopref.parsing.implementations.model.SubFactorInfo

object SubparaReferences {

  val hasSubRefsRegex = """([0-9]+\([0-9]+\))(\([a-z]+\))""".r
  def hasSubParas(wholeRef : String) = {
      hasSubRefsRegex.findFirstIn(wholeRef).isDefined
  }

  def splitNewStyleSopSubParaReference(wholeRef : String) = {
    // take to end of first )
    assert (hasSubParas(wholeRef))
    val m = hasSubRefsRegex.findFirstMatchIn(wholeRef).get
    (m.group(1),m.group(2))
  }


  def tryGetSubFactor(subFactorLetter : String, wholeRef : String) : String = {
      val parts = splitNewStyleSopSubParaReference(wholeRef);
      null
  }
}
