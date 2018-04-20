package au.gov.dva.sopapi.veaops
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Optional

import com.google.common.collect.ImmutableSet

import scala.collection.immutable
import scala.xml._

class VeaOperation(val name : String, val startDate : LocalDate, val endDate : Option[LocalDate], val specifiedAreas: List[SpecifiedArea], val qualifications : List[Qualification]) extends HasDates

class SpecifiedArea(val desc : String)

class Qualification(val text: String)

class VeaActivity(val startDate : LocalDate, val endDate : Option[LocalDate], val specifiedAreas : List[SpecifiedArea], val qualifications : List[Qualification]) extends HasDates

abstract class VeaDetermination(val registerId : String, val operations : List[VeaOperation], val activities : List[VeaActivity])

case class WarlikeDetermination(override val registerId: String, override val operations : List[VeaOperation], override val activities : List[VeaActivity]) extends VeaDetermination(registerId,operations,activities)

case class NonWarlikeDetermination(override val registerId: String, override val operations : List[VeaOperation], override val activities : List[VeaActivity]) extends VeaDetermination(registerId,operations,activities)

case class HazardousDetermination(override val registerId: String, override val operations : List[VeaOperation], override val activities : List[VeaActivity]) extends VeaDetermination(registerId,operations,activities)

trait HasDates {
  def startDate : LocalDate
  def endDate : Option[LocalDate]
}

object VeaOperationQueries {
  def getDeterminationsOnDate(testDate: LocalDate, allDeterminations: List[VeaDetermination]): Map[VeaDetermination, List[HasDates]] =
  {

     def getThingsAtDate(testDate: LocalDate, things : List[HasDates]) : List[HasDates] =
       things
      .filter(o => !o.startDate.isAfter(testDate) && (o.endDate.isEmpty || !o.endDate.get.isBefore(testDate)))


     val ongoingAtDate = allDeterminations
                  .map(d => d -> (getThingsAtDate(testDate, d.operations) ++ getThingsAtDate(testDate,d.activities)))
                    .filter(i => !i._2.isEmpty)
                  .toMap

     ongoingAtDate
  }
}

object VeaDeserialisationUtils {

  def specifiedAreas(specifiedAreasNode : scala.xml.Node) : List[SpecifiedArea] =
     (specifiedAreasNode \ "specifiedAreas" \ "specifiedArea").map(i => new SpecifiedArea(i.text)).toList
  def qualifications(qualificationsNode : scala.xml.Node) : List[Qualification] =
    (qualificationsNode \ "qualifications" \ "qualification").map(i => new Qualification(i.text)).toList

  def OperationFromXml(node: scala.xml.Node) : VeaOperation = {
    val name = (node \ "name").text
    val startDate =  LocalDate.parse((node \ "startDate").text,DateTimeFormatter.ISO_LOCAL_DATE)
    val endDate =  (node \ "endDate").headOption.map(i => LocalDate.parse(i.text,DateTimeFormatter.ISO_LOCAL_DATE))
    val specifiedAreas = VeaDeserialisationUtils.specifiedAreas(node)
    val qualifications =  VeaDeserialisationUtils.qualifications(node)
    new VeaOperation(name,startDate,endDate,specifiedAreas,qualifications)
  }

  def ActivityFromXml(node: scala.xml.Node) : VeaActivity = {

    val startDate =  LocalDate.parse((node \ "startDate").text,DateTimeFormatter.ISO_LOCAL_DATE)
    val endDate =  (node \ "endDate").headOption.map(i => LocalDate.parse(i.text,DateTimeFormatter.ISO_LOCAL_DATE))
    val specifiedAreas = VeaDeserialisationUtils.specifiedAreas(node)
    val qualifications =  VeaDeserialisationUtils.qualifications(node)
    new VeaActivity(startDate,endDate,specifiedAreas,qualifications)
  }


  def DeterminationsfromXml(node: scala.xml.Node) : List[VeaDetermination] =
  {
    def getData(determinationNode: scala.xml.Node) = {
      val registerId = (determinationNode \  "federalRegisterOfLegislationReference").text
      val operations = (determinationNode \ "operations" \ "operation").map(o => VeaDeserialisationUtils.OperationFromXml(o)).toList
      val activities = (determinationNode \ "activities" \ "activity" ).map(a => VeaDeserialisationUtils.ActivityFromXml(a)).toList
      (registerId,operations,activities)
    }


    val warlikeDeterminationElements = node \ "warlikeAndNonWarlike" \ "warlikeDeterminations" \ "determination"
    val warlike: immutable.Seq[WarlikeDetermination] = warlikeDeterminationElements.map(d => getData(d)).map(o => WarlikeDetermination(o._1, o._2, o._3)).toList

    val nonWarlikeDeterminations = (node \ "warlikeAndNonWarlike" \ "non-warlikeDeterminations" \ "determination").map(d => getData(d)).map(o => NonWarlikeDetermination(o._1, o._2, o._3)).toList

    val hazardous = (node \ "hazardous" \ "determinations" \ "determination").map(h => getData(h)).map(o => HazardousDetermination(o._1,o._2,o._3))

    (warlike ++ nonWarlikeDeterminations ++ hazardous).toList
  }

}




