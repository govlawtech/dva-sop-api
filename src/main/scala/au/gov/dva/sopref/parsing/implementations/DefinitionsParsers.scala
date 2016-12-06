package au.gov.dva.sopref.parsing.implementations

import au.gov.dva.sopref.exceptions.SopParserError

import scala.collection.mutable.ListBuffer

object DefinitionsParsers {

  def splitToDefinitions(definitionsSection : String) : List[String] = {
     assert(!definitionsSection.startsWith("\""))
    val acc = List[String]();
     val lines = definitionsSection.split("[\r\n]+").toList.drop(1)
     val result: List[String] = divideRecursive(acc,lines)
     return result

  }

  private def divideRecursive(divided : List[String], toDivide: List[String]) : List[String] = {

    if (toDivide.isEmpty)
      return (divided)
    else {
      assert(toDivide.head.startsWith("\""))
      val definitionLines = (toDivide.head :: toDivide.tail.takeWhile(s => !s.startsWith("\"")))
      val definition = definitionLines.mkString("\n")
      divideRecursive(definition :: divided,toDivide.drop(definitionLines.size))
    }
  }

  def parseSingleDefinition(definition : String) : (String,String) = {
    val definedWordRegex = """"[A-Za-z\-\s0-9']+"""".r
    val m = definedWordRegex.findFirstMatchIn(definition)
    if (m.isEmpty)
      throw new SopParserError("Cannot find the defined word in this definition: " + definition)
    val toTrim = m.get.matched.size;
    var remainder = definition.drop(toTrim).trim
    var word = m.get.matched.drop(1).dropRight(1)
    return (word,remainder)
  }

}
