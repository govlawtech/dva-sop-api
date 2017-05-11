package au.gov.dva.sopapi.sopref.parsing.traits

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.exceptions.SopParserError
import au.gov.dva.sopapi.sopref.parsing.SoPExtractorUtilities
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, FactorInfoWithSubParas, FactorInfoWithoutSubParas}

import scala.util.Properties
import scala.util.parsing.combinator.RegexParsers


trait PreAug2015FactorsParser extends RegexParsers with BodyTextParsers with TerminatorParsers {

  // main para letter, list of sub paras with text, optional tail
  def mainParaLetter: Parser[String] =
    """\(([a-z])+\)""".r

  def head: Parser[String] = """[a-z\s]+""".r <~ """[,:\]""".r

  def subParaLetter: Parser[String] = """\([ixv]+\)""".r

  def subPara: Parser[(String, String, Option[String])] = subParaLetter ~ subParaBodyText ~ opt(andTerminator | orTerminator | semiColonTerminator) ^^ {
    case letter ~ body ~ terminator => (letter, body, terminator)
  }


  def tailParser: Parser[String] = not(orTerminator) ~> """[a-z,\s]+""".r <~ (periodTerminator | orTerminator)

  def singleLevelPara: Parser[FactorInfoWithoutSubParas] = mainParaLetter ~ mainFactorBodyText <~ opt(orTerminator | periodTerminator) ^^ {
    case para ~ text => new FactorInfoWithoutSubParas(para, text)
  }


  def factorHead: Parser[(String, String)] = mainParaLetter ~ mainFactorBodyText <~ opt(":" | ",") ^^ {
    case letter ~ body => (letter, body)
  }

  private def toLineList(stringWithLinebreaks : String) = {
    stringWithLinebreaks.split(Properties.lineSeparator).toList
  }

  private def flattenLineBreaks(lines : List[String]) = {
    lines.mkString(" ")
  }

  private def flattenLineBreaks(stringWithLineBreaks : String) : String = {
    flattenLineBreaks(toLineList(stringWithLineBreaks))
  }

  private def replaceLineBreaksWithSpace(stringWithLineBreaks: String) : String = {
    stringWithLineBreaks.replace(Properties.lineSeparator," ")
  }

  def parseSingleFactor(singleFactorTextInclLineBreaks: String): FactorInfo = {

    val (headOrAll, rest) = SoPExtractorUtilities.splitFactorToHeaderAndRest(
      toLineList(singleFactorTextInclLineBreaks))
    assert(!headOrAll.isEmpty)

    if (rest.isEmpty) {
      val simpleFactorParseResult = this.parseAll(this.singleLevelPara, headOrAll)
      if (!simpleFactorParseResult.successful) throw new SopParserError(simpleFactorParseResult.toString)
      else return simpleFactorParseResult.get
    }

    val headParseResult = this.parseAll(this.factorHead, headOrAll)
    if (!headParseResult.successful) {
      throw new SopParserError(headParseResult.toString)
    }

    val restSplitToSubParas = SoPExtractorUtilities.splitFactorToSubFactors(rest)
      .map(i => i.mkString(Properties.lineSeparator))

    val subFactorTextsExceptLast = restSplitToSubParas.dropRight(1)
      .map(t => flattenLineBreaks(t))
    val parseSubFactorsExceptLast = subFactorTextsExceptLast
      .map(this.parseAll(this.subPara,_ ))

    if (parseSubFactorsExceptLast.exists(p => !p.successful)) {
      val unsuccessful = parseSubFactorsExceptLast.filter(!_.successful)
      val msg = unsuccessful.map(us => us.toString).mkString(Properties.lineSeparator)
      throw new SopParserError(msg)
    }
    else {

      val (lastPara, tailLines) = SoPExtractorUtilities.splitOutTailIfAny(restSplitToSubParas.takeRight(1).head)

      val lastParaParseResult = this.parseAll(this.subPara, flattenLineBreaks(lastPara))
      if (!lastParaParseResult.successful) {
        throw new SopParserError(lastParaParseResult.toString)
      }

      val headLetter = headParseResult.get._1
      val headText = headParseResult.get._2
      val allSubParaInfosButLast: List[(String, String, Option[String])] = parseSubFactorsExceptLast.map(r => r.get)
      val lastSubParaInfoResult: (String, String, Option[String]) = lastParaParseResult.get
      val allSubParas = allSubParaInfosButLast :+ lastSubParaInfoResult

      if (tailLines.isDefined) {
        val parsedTail: ParseResult[String] = this.parseAll(this.tailParser, replaceLineBreaksWithSpace(tailLines.get))
        if (!parsedTail.successful) throw new SopParserError(parsedTail.toString)

        return new FactorInfoWithSubParas(headLetter, headText, allSubParas, Some(parsedTail.get))
      }
      else return new FactorInfoWithSubParas(headLetter, headText, allSubParas, None)
    }
  }

  def parseFactorsSection(factorsSectionText: String): (StandardOfProof, List[FactorInfo]) = {
    val splitToLines: List[String] = toLineList(factorsSectionText)
    val (header: String, rest: List[String]) = SoPExtractorUtilities.splitFactorsSectionToHeaderAndRest(splitToLines)

    val groupedToCollectionsOfFactors: List[String] = SoPExtractorUtilities.splitFactorsSectionByFactor(rest)
      .map(factorLineCollection => factorLineCollection.mkString(Properties.lineSeparator))

    assert(groupedToCollectionsOfFactors.forall(i => !i.endsWith(" ") && !i.startsWith(" ")))

    val parsedFactors = groupedToCollectionsOfFactors
      .map(parseSingleFactor(_))

    val standard = extractStandardOfProofFromHeader(header)
    (standard, parsedFactors)
  }

  private def extractStandardOfProofFromHeader(headerText: String): StandardOfProof = {
    if (headerText.contains("balance of probabilities"))
      return StandardOfProof.BalanceOfProbabilities
    if (headerText.contains("reasonable hypothesis"))
      return StandardOfProof.ReasonableHypothesis
    else {
      throw new SopParserError("Cannot determine standard of proof from text: " + headerText)
    }
  }

}





