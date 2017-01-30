package au.gov.dva.sopapi.sopref.parsing

import java.time.{LocalDate, OffsetDateTime}

import au.gov.dva.sopapi.DateTimeUtils
import au.gov.dva.sopapi.exceptions.ServiceDeterminationParserError
import au.gov.dva.sopapi.interfaces.model.{ServiceDetermination, ServiceType}
import au.gov.dva.sopapi.sopref.data.servicedeterminations.StoredServiceDetermination
;

object ServiceDeterminationsParser {
  def getRegisterId(determinationText : String) : String  =  {
    val registerIdRegex = """F[0-9]{4,4}L[0-9]{5,5}""".r
    val m = registerIdRegex.findFirstMatchIn(determinationText)
    if (m.isEmpty)
      {
        throw new ServiceDeterminationParserError(s"Could not determine register Id from determination: $determinationText")
      }
    m.get.matched
  }

  def getRegisteredDate(determinationsText : String) : Option[OffsetDateTime] = {

    val dateMatched = """registered ([0-9]{1,2})/([0-9]{1,2})/([0-9]{4,4})""".r
    val m = dateMatched.findFirstMatchIn(determinationsText)
    if (m.isEmpty)
      None
    else {
      val year = m.get.group(3).toInt
      val month = m.get.group(2).toInt
      val day = m.get.group(1).toInt
      Some( DateTimeUtils.localDateToMidnightACTDate(LocalDate.of(year,month,day)))
    }
  }

  def getCitation(determinationText : String) : String = {
    val regex = """Military\s+Rehabilitation\s+and\s+Compensation\s+\((Warlike|Non.warlike)\s+Service\)\s+Determination [0-9]{4,4}\s+\(No.?\s+[0-9]+\)""".r

      val m = regex.findFirstMatchIn(determinationText);
    if (m.isEmpty)
      throw new ServiceDeterminationParserError(s"Could not extract citation from: $determinationText")

    val lineBreak = """[\r\n]+""".r
    lineBreak.replaceAllIn(m.get.matched," ")
  }

  def getServiceTypeFromCitation(citation: String) : ServiceType = {
    if (!"""Non.[Ww]arlike""".r.findFirstMatchIn(citation).isEmpty)
      return ServiceType.NON_WARLIKE
    else ServiceType.WARLIKE
  }

  def createServiceDetermination(docx : Array[Byte], plainText: String) : ServiceDetermination = {
    val operations = au.gov.dva.sopapi.sopref.data.ServiceDeterminations.extractOperations(docx);

    val commencementDate: OffsetDateTime = {
      val commencementDateFromDocX = au.gov.dva.sopapi.sopref.data.ServiceDeterminations.extractCommencementDateFromDocx(docx);
      if (commencementDateFromDocX.isPresent)
        commencementDateFromDocX.get()
      else {
        val registeredDateFromText = getRegisteredDate(plainText)
        if (registeredDateFromText.isEmpty)
        {
          throw new ServiceDeterminationParserError(s"Cannot determine commencement date for Service Determination: $plainText")
        }
        else {
          registeredDateFromText.get
        }
      }
    }

    val registerId = getRegisterId(plainText)

    val citation = getCitation(plainText)

    val serviceType = getServiceTypeFromCitation(citation);

    return new StoredServiceDetermination(
      registerId,
      citation,
      commencementDate,
      operations,
      serviceType);

  }



}