package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import au.gov.dva.sopapi.exceptions.SopParserError

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
        .mkString(Properties.lineSeparator)
      divideRecursive(definitionLines :: divided,toDivide.drop(definitionLines.size))
    }
  }

  private def lineIsDefinitionStart(line : String) = """\s?.*(means|includes).*$""".r.findFirstIn(line).isDefined

  def parseSingleDefinition(definition : String) : (String,String) = {




    val defRegex = """[\s.]*(means|includes)([\r\n.\s]*)$""".r
    val m = defRegex.findFirstIn(definition)

    if (m.isEmpty)
      throw new SopParserError("Cannot find the defined word in this definition: " + definition)

    //val term = m.get.group(1).trim
    //val meaning = m.get.group(3).stripPrefix(":")

    return null
  }

}
