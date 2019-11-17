package au.gov.dva.sopapi.sopref.parsing.implementations.model

import au.gov.dva.sopapi.interfaces.model.ConditionVariant

import scala.util.Properties

// intermediate objects created by parsing SoP text
abstract class FactorInfo  {
  def getLetter : String
  def getText : String
  def getConditionVariant : Option[ConditionVariant] = None
}

class FactorInfoForFactorSectionWithOnlyOneFactor(factorText: String)  extends FactorInfo
{
  override def getLetter: String = ""
  override def getText: String = factorText
}

class FactorInfoWithoutSubParas(mainParaLetter : String, bodyText: String) extends FactorInfo
{
  override def getLetter: String = mainParaLetter.toLowerCase()
  override def getText: String = bodyText.stripSuffix(";").stripSuffix(".")
  override def toString: String = "Letter: " +  getLetter + Properties.lineSeparator + "Text: " + bodyText
}

abstract class ConditionVariantInfo
{
  def getConditionVariantName : String
  def getConditionVariantFactors: List[SubFactorInfo]
}

case class BasicConditionVariantInfo(name: String, variantFactors : List[SubFactorInfo]) extends ConditionVariantInfo {
  override def getConditionVariantName: String = name
  override def getConditionVariantFactors: List[SubFactorInfo] = variantFactors
}










