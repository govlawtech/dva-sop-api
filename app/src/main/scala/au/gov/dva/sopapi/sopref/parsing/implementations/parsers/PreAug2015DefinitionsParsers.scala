package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import au.gov.dva.sopapi.exceptions.SopParserRuntimeException

object PreAug2015DefinitionsParsers {

  private val defKeyWordRegex = """(means:?|includes:?)""".r

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
      if (!toDivide.head.startsWith("\"")) throw new SopParserRuntimeException("Defined term expected to start with quotation mark.")
      val definitionLines = (toDivide.head :: toDivide.tail.takeWhile(s => !s.startsWith("\"")))
      val definition = definitionLines.mkString("\n")
      divideRecursive(definition :: divided,toDivide.drop(definitionLines.size))
    }
  }

  def parseSingleDefinition(definition : String) : (String,String) = {

    val wordsBeforeMeans = definition.split("""(\s|(\n|\r\n))""").takeWhile(w => defKeyWordRegex.findFirstIn(w).isEmpty).mkString(" ")
    val m = wordsBeforeMeans
    if (m.isEmpty)
      throw new SopParserRuntimeException("Cannot find the defined word in this definition: " + definition)
    val toTrim = m.size
    var remainder = definition.drop(toTrim).trim.stripSuffix(";").stripSuffix(".")
    var word = m.drop(1).dropRight(1)
    return (word,remainder)
  }

}
