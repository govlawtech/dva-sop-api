package au.gov.dva.sopapi.sopref.parsing.implementations.model

import scala.util.Properties

// intermediate objects created by parsing SoP text
abstract class FactorInfo {
  def getLetter : String
  def getText : String
}

class FactorInfoForFactorSectionWithOnlyOneFactor(factorText: String) extends FactorInfo
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









