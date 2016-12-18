package au.gov.dva.sopapi.sopref.parsing.implementations

import au.gov.dva.sopapi.interfaces.model.InstrumentNumber


class ParsedInstrumentNumber(number: Int, year: Int) extends InstrumentNumber {
  override def getNumber: Int = number
  override def getYear: Int = year

}
