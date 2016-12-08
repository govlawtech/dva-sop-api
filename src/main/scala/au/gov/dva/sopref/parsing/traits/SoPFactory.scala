package au.gov.dva.sopref.parsing.traits

import au.gov.dva.sopref.interfaces.model.{DefinedTerm, Factor, SoP}
import au.gov.dva.sopref.parsing.implementations.ParsedFactor

trait SoPFactory {
  def create(registerId : String, clensedText : String) : SoP

  def splitFactors(parasInOrder : List[String], startPara : String, endPara : String) = {
    val onsetParas = parasInOrder.takeWhile(i => i != startPara) ++
      (parasInOrder.reverse.takeWhile(i => i != endPara)).reverse
    val aggParas = parasInOrder.filter(p => !onsetParas.contains(p))
    (onsetParas,aggParas)
  }

  def buildFactorObjects(factorData : List[(String,String)], factorSectionNumber : Int, definedTerms :  List[DefinedTerm]) : List[Factor] = {

    factorData
      .map(f => (factorSectionNumber.toString.concat(f._1),f._2)) // prepend para number to letter
      .map(i =>
        { val relevantDefinitions = definedTerms.filter(d => i._2.contains(d.getTerm)).toSet
          new ParsedFactor(i._1,i._2,relevantDefinitions)
        }
      )
  }
}
