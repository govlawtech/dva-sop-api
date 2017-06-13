package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import au.gov.dva.sopapi.exceptions.SopParserRuntimeException

import scala.util.Properties

object PostAug2015DefinitionsParsers {

  def splitToDefinitions(definitionsSection : String) : List[String] = {

    val acc = List[String]();
    val lines = definitionsSection.split("[\r\n]+").toList.drop(1)
    val result: List[String] = divideRecursive(acc,lines)
    return result

  }

  private def divideRecursive(divided : List[String], toDivide: List[String]) : List[String] = {
    if (toDivide.isEmpty)
      return (divided)
    else {
      val definitionLines = (toDivide.head :: toDivide.tail.takeWhile(s => !lineIsDefinitionStart(s)))
      divideRecursive(definitionLines.mkString(Properties.lineSeparator) :: divided,toDivide.drop(definitionLines.size))
    }
  }

  private def lineIsDefinitionStart(line : String) = """\s*[a-zA-Z\s]+\s(means|includes)""".r.findFirstIn(line).isDefined

  def parseSingleDefinition(definition : String) : (String,String) = {
    val defKeyWordRegex = """(means|includes):?""".r

    val words = definition.split("\\s").map(i => i.trim());
    val wordsToMeans = words.takeWhile(w => defKeyWordRegex.findFirstIn(w).isEmpty).mkString(" ").trim()
    val wordsWithoutDefinedTerm = definition.replace(wordsToMeans,"").trim
    (wordsToMeans,wordsWithoutDefinedTerm)
  }

}
