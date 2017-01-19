package au.gov.dva.sopapi.sopref.parsing.implementations

import java.text.ParseException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.exceptions.SopParserError
import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, InstrumentNumber}
import au.gov.dva.sopapi.sopref.parsing.traits.SoPParser

import scala.collection.immutable.Seq
import scala.util.parsing.combinator.RegexParsers
import com.frequal.romannumerals.Converter

object OsteoarthritisParser extends SoPParser with RegexParsers {


  def orTerminator: Parser[String] = """;\s+or""".r

  def andTerminator: Parser[String] = """;\s+and""".r

  def periodTerminator: Parser[String] = """\.$""".r

  def semicolonTerminator: Parser[String] = """;""".r

  def factorTerminatorParser: Parser[String] = orTerminator | periodTerminator

  def subParaBodyTextParser: Parser[String] = """[A-Za-z0-9\-'’,\s]+[A-Za-z0-9]""".r
  def subParaLetterParser: Parser[String] = """\([a-z]+\)""".r
  def paraHeadParser: Parser[String] = """[A-Za-z0-9\-'’,\s]+,\s""".r

  def subParaSeparatorParser: Parser[String] = andTerminator | orTerminator

  def finalSubParaParser: Parser[(String, String)] = subParaLetterParser ~ subParaBodyTextParser ^? {
    case subParaLetter ~ subParaBody if (verifySubparagraphLabel(subParaLetter)) => { currentSubparagraphNumber+=1; (subParaLetter, subParaBody) }
  }

  def subParaParser: Parser[(String, String)] = subParaLetterParser ~ subParaBodyTextParser ~ subParaSeparatorParser ^? {
    case subParaLetter ~ subParaBody ~ subParaSeparator if (verifySubparagraphLabel(subParaLetter)) => { currentSubparagraphNumber+=1; (subParaLetter, subParaBody + subParaSeparator) }
  }

  def listSubParasParser: Parser[List[(String, String)]] = rep1(subParaParser) ~ finalSubParaParser ^^ {
    case listOfSubFactors ~ finalSubPara => listOfSubFactors :+ finalSubPara
  }

  def paraFooterParser: Parser[String] = semicolonTerminator ~> paraFooterTextParser

  def paraFooterTextParser: Parser[String] = """[A-Za-z0-9\-'’,\s]+[A-Za-z0-9]""".r


  def factorBodyTextParser: Parser[(String, List[(String, String)], String)] = """(([A-Za-z0-9\-'’,\)\(\s](?!\(i\))|\.(?=[A-Za-z0-9])(?!\(i\))))+""".r ^^ {
    case factorBody => (factorBody, null, null)
  }


  def paraWithSubParasParser: Parser[(String, List[(String, String)], String)] = paraHeadParser ~ listSubParasParser ~ paraFooterParser.? ^^ {
    case head ~ subFactorList ~ footer => currentSubparagraphNumber=0; (head, subFactorList, footer.orNull)
  }

  var currentSubparagraphNumber : Int = 0

  def verifySubparagraphLabel(label : String) : Boolean = {
    def strippedLabel = label.replace("(", "").replace(")", "").toUpperCase()
    try {
        var number = new Converter().toNumber(strippedLabel)
        return number == currentSubparagraphNumber + 1
    } catch {
      case ex: ParseException => false
    }
  }

  def paraBodyParser: Parser[(String, List[(String, String)], String)] = paraWithSubParasParser | factorBodyTextParser

  def paraLetterParser: Parser[String] = """\([a-z]+\)""".r

  def paraAndTextParser: Parser[(String, String, List[(String, String)], String)] = paraLetterParser ~ paraBodyParser <~ factorTerminatorParser ^^ {
    case para ~ factorComponents => (para, factorComponents._1, factorComponents._2, factorComponents._3)
  }

  def separatedFactorListParser: Parser[List[(String, String, List[(String, String)], String)]] = rep1(paraAndTextParser) ^^ {
    case listOfFactors: Seq[(String, String)] => listOfFactors
  }

  def bodyTextParser: Parser[String] = """(([A-Za-z0-9\-'’,\)\(\s]|\.(?=[A-Za-z0-9])))+""".r

  def headParser: Parser[String] = bodyTextParser <~ ":"


  def completeFactorSectionParser: Parser[(String, List[(String, String, List[(String, String)], String)])] = headParser ~ separatedFactorListParser <~ periodTerminator ^^ {
    case head ~ factorList => (head, factorList)
  }

  override def parseFactors(factorsSection: String): (StandardOfProof, List[(String, String)]) = {
    val result = this.parseAll(this.completeFactorSectionParser, factorsSection);
    if (!result.successful)
      throw new SopParserError(result.toString)
    else {
      val standardOfProof = extractStandardOfProofFromHeader(result.get._1)
      //(standardOfProof, result.get._2)
      (standardOfProof, null)
    }
  }

  def extractStandardOfProofFromHeader(headerText: String): StandardOfProof = {
    if (headerText.contains("balance of probabilities"))
      return StandardOfProof.BalanceOfProbabilities
    if (headerText.contains("reasonable hypothesis"))
      return StandardOfProof.ReasonableHypothesis
    else {
      throw new SopParserError("Cannot determine standard of proof from text: " + headerText)
    }
  }


  override def parseInstrumentNumber(citationSection: String): InstrumentNumber = {
    val instrumentNumberRegex = """No\.?\s+([0-9]+)\s+of\s+([0-9]{4,4})""".r
    val regexMatch = instrumentNumberRegex.findFirstMatchIn(citationSection);
    if (regexMatch.isEmpty)
      throw new SopParserError("Cannot determine instrument number from this citation: " + citationSection)

    val number = regexMatch.get.group(1).toInt
    val year = regexMatch.get.group(2).toInt
    new ParsedInstrumentNumber(number.toInt, year.toInt);
  }

  override def parseDefinitions(definitionsSection: String): List[DefinedTerm] = {
    DefinitionsParsers.splitToDefinitions(definitionsSection)
      .map(DefinitionsParsers.parseSingleDefinition(_))
      .map(t => new ParsedDefinedTerm(t._1, t._2))
  }


  override def parseDateOfEffect(dateOfEffectSection: String): LocalDate = {
    val doeRegex = """effect from ([0-9]+\s+[A-Za-z]+\s+[0-9]{4,4})""".r
    val m = doeRegex.findFirstMatchIn(dateOfEffectSection)
    if (m.isEmpty)
      throw new SopParserError("Cannot determine date of effect from: " + dateOfEffectSection)
    return LocalDate.parse(m.get.group(1), DateTimeFormatter.ofPattern("d MMMM yyyy"))
  }

  override def parseStartAndEndAggravationParas(aggravationSection: String): (String, String) = {
    val paraIntervalRegex = """Paragraphs [0-9]+(\([a-z]+\)) to [0-9]*(\([a-z]+\))""".r
    val m = paraIntervalRegex.findFirstMatchIn(aggravationSection)
    if (m.isEmpty)
      throw new SopParserError("Cannot determine aggravation paras from: " + aggravationSection)
    (m.get.group(1), m.get.group(2))
  }

  override def parseCitation(citationSection: String): String = {
    val regex = """may be cited as (.*)""".r
    val m = regex.findFirstMatchIn(citationSection)
    if (m.isEmpty)
      throw new SopParserError("Cannot get citation from: " + citationSection)
    val trimmed = m.get.group(1).stripSuffix(".")
    trimmed
  }

  override def parseConditionNameFromCitation(citation: String): String = {
    val regex = """Statement of Principles concerning (([A-Za-z-'\s](?!No\.))*)""".r

    val m = regex.findFirstMatchIn(citation);
    if (m.isEmpty)
      throw new SopParserError("Cannot get condition name from this citation: %s".format(citation))
    return m.get.group(1);

  }
}
