package au.gov.dva.sopapi.sopref.parsing.traits

trait SubParasHandler {
  def getSubParaReferenceSplitter : ParaReferenceSplitter
  def getSubParaParser : SubFactorParser
}
