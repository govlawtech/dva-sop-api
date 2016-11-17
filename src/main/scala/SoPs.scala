package au.gov.dva.sopref

import java.time.LocalDate

import au.gov.dva.sopref.interfaces.model.InstrumentNumber

trait SoPLexer {
  def extractFactorSection(plainTextSop : String) : String
  def extractDefinitionsSection(plainTextSop : String) : String
  def extractCommencementSection(plainTextSop : String) : String
  def extractCitation(plainTextSop : String) : String
}

trait SoPParser {
  def parseFactors(factorSection: String): Map[String, String]
  def parseDefinitions(definitionSection: String): Map[String, String]
  def parseCommencementDate(commencementSection: String): LocalDate
  def parseInstrumentNumber(plainTextSop : String) : InstrumentNumber
}
