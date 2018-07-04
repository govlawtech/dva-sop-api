package au.gov.dva.sopapi.veaops.interfaces

import java.time.LocalDate

trait HasDates {
  def startDate: LocalDate
  def endDate: Option[LocalDate]
}
