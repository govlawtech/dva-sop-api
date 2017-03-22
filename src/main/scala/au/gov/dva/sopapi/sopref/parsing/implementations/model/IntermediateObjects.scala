package au.gov.dva.sopapi.sopref.parsing.implementations.model

import scala.util.Properties

// intermediate objects created by parsing SoP text
abstract class FactorInfo {
  def getLetter : String
  def getText : String
}

class FactorInfoWithoutSubParas(mainParaLetter : String, bodyText: String) extends FactorInfo
{
  override def getLetter: String = mainParaLetter.toLowerCase()

  override def getText: String = bodyText

  override def toString: String = getLetter + " " + bodyText
}


class FactorInfoWithSubParas(mainParaLetter : String, head : String, subParas : List[(String,String, Option[String])], tail :  Option[String]) extends FactorInfo
{

  private def format = {
    assert(!head.contains(Properties.lineSeparator))
    val lastSubParaWithoutTerimator: (String, String, Option[String]) = (subParas.last._1, subParas.last._2, Some(""))
    assert(!lastSubParaWithoutTerimator._1.contains(Properties.lineSeparator))
    val allButLast: List[(String, String, Option[String])] = subParas.take(subParas.size - 1)
    val all: List[(String, String, Option[String])] = allButLast :+ lastSubParaWithoutTerimator
    val formatted =  head + Properties.lineSeparator + (all.map(formatSubPara(_)).mkString(Properties.lineSeparator)) + formatTail(tail)
    formatted
  }

  private def formatSubPara(subPara : (String,String,Option[String])) = {
    subPara._1 + " " + subPara._2 + subPara._3.getOrElse(";")
  }

  private def formatTail(tailOption : Option[String]) = if (tailOption.isDefined) ";" + Properties.lineSeparator + tailOption.get
  else "";

  override def toString: String = format

  override def getLetter: String = mainParaLetter.toLowerCase()

  override def getText: String = this.format
}









