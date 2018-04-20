package au.gov.dva.sopapi.veaops
import java.time.{LocalDate, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.Optional

import com.google.common.collect.ImmutableSet
import org.joda.time.Interval

import scala.collection.immutable
import scala.xml._

class VeaOperation(val name : String, val startDate : LocalDate, val endDate : Option[LocalDate], val specifiedAreas: List[SpecifiedArea], val qualifications : List[Qualification]) extends VeaOccurance {
  override def toString: String = name
}

class SpecifiedArea(val desc : String)

class Qualification(val text: String)

class VeaActivity(val startDate : LocalDate, val endDate : Option[LocalDate], val specifiedAreas : List[SpecifiedArea], val qualifications : List[Qualification]) extends VeaOccurance

abstract class VeaDetermination(val registerId : String, val operations : List[VeaOperation], val activities : List[VeaActivity])

case class WarlikeDetermination(override val registerId: String, override val operations : List[VeaOperation], override val activities : List[VeaActivity]) extends VeaDetermination(registerId,operations,activities) {
  override def toString: String = s"$registerId: warlike, ${operations.length} ops, ${activities.length} activities"
}

case class NonWarlikeDetermination(override val registerId: String, override val operations : List[VeaOperation], override val activities : List[VeaActivity]) extends VeaDetermination(registerId,operations,activities)
{

  override def toString: String = s"$registerId: non-warlike, ${operations.length} ops, ${activities.length} activities"
}

case class HazardousDetermination(override val registerId: String, override val operations : List[VeaOperation], override val activities : List[VeaActivity]) extends VeaDetermination(registerId,operations,activities)
{
  override def toString: String = s"$registerId: hazardous, ${operations.length} ops, ${activities.length} activities"
}

trait HasDates {
  def startDate : LocalDate
  def endDate : Option[LocalDate]
}

trait VeaOccurance extends HasDates {

}

object VeaOperationQueries {
  def getOpsAndActivitiesOnDate(testDate: LocalDate, allDeterminations: List[VeaDetermination]): Map[VeaDetermination, List[VeaOccurance]] =
  {
     def getThingsAtDate(testDate: LocalDate, things : List[VeaOccurance]) : List[VeaOccurance] =
       things
      .filter(o => !o.startDate.isAfter(testDate) && (o.endDate.isEmpty || !o.endDate.get.isBefore(testDate)))

     val ongoingAtDate = allDeterminations
                  .map(d => d -> (getThingsAtDate(testDate, d.operations) ++ getThingsAtDate(testDate,d.activities)))
                    .filter(i => !i._2.isEmpty)
                  .toMap

     ongoingAtDate
  }

  def getOpsAndActivitiesInRange(startOfTestRange: LocalDate, endOfTestRange : LocalDate, allDeterminations: List[VeaDetermination]): List[VeaOccurance] =
  {
     allDeterminations.flatMap(d => getVeaOccurancesInInterval(startOfTestRange,endOfTestRange,d))
  }

  private def getVeaOccurancesInInterval(startTestDate : LocalDate, endTestDate: LocalDate, veaDetermination: VeaDetermination) = {
    val occurances = veaDetermination.activities ++ veaDetermination.operations
    val inRange: List[VeaOccurance] = occurances.filter(o => intervalsOverlap(startTestDate,endTestDate,o))
    inRange
  }

  private def intervalsOverlap(startTestDate: LocalDate, endTestDate: LocalDate, potentiallyOpenEndedInterval: HasDates): Boolean = {
    if (potentiallyOpenEndedInterval.endDate.isEmpty) {
      !endTestDate.isBefore(potentiallyOpenEndedInterval.startDate)
    }
    else { // closed ended
      !startTestDate.isAfter(potentiallyOpenEndedInterval.endDate.get) && !endTestDate.isBefore(potentiallyOpenEndedInterval.startDate)
    }
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
      val activities = (determinationNode \ "activities" \ "activity").map(a => VeaDeserialisationUtils.ActivityFromXml(a)).toList
      (registerId,operations,activities)
    }

    val warlikeDeterminationElements = node \ "warlikeAndNonWarlike" \ "warlikeDeterminations" \ "determination"
    val warlike: immutable.Seq[WarlikeDetermination] = warlikeDeterminationElements.map(d => getData(d)).map(o => WarlikeDetermination(o._1, o._2, o._3)).toList

    val nonWarlikeDeterminations = (node \ "warlikeAndNonWarlike" \ "non-warlikeDeterminations" \ "determination").map(d => getData(d)).map(o => NonWarlikeDetermination(o._1, o._2, o._3)).toList

    val hazardous = (node \ "hazardous" \ "determinations" \ "determination").map(h => getData(h)).map(o => HazardousDetermination(o._1,o._2,o._3))

    (warlike ++ nonWarlikeDeterminations ++ hazardous).toList
  }
}




