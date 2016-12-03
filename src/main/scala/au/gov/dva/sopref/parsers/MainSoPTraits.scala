package au.gov.dva.sopref.parsers

import java.time.LocalDate

import au.gov.dva.sopref.interfaces.model.{InstrumentNumber, SoP}

trait SoPParser {
  def parse(rawText : String) : SoP
}

trait SoPClenser {
  def clense(rawText : String) : String;
}

trait SoPExtractor {
  def extractFactorSection(plainTextSop : String) : String
  def extractDefinitionsSection(plainTextSop : String) : String
  def extractCommencementSection(plainTextSop : String) : String
  def extractCitation(plainTextSop : String) : String
}

trait SoPElementsParser {
  def parseFactors(factorSection: String): Map[String, String]
  def parseDefinitions(definitionSection: String): Map[String, String]
  def parseCommencementDate(commencementSection: String): LocalDate
  def parseInstrumentNumber(plainTextSop : String) : InstrumentNumber
}




