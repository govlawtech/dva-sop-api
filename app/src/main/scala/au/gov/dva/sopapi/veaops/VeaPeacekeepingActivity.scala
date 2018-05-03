package au.gov.dva.sopapi.veaops

import java.time.LocalDate

import scala.util.matching.Regex

case class  VeaPeacekeepingActivity(shortName: String, desc: String, initialDate: LocalDate, legalSource: String, regexMappings : Set[Regex])
