package au.gov.dva.sopapi.veaops
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Optional

import com.google.common.collect.ImmutableSet

import scala.collection.immutable
import scala.xml._

class VeaOperation(val name : String, val startDate : LocalDate, val endDate : Option[LocalDate], val specifiedAreas: List[SpecifiedArea], val qualifications : List[Qualification])

class SpecifiedArea(val desc : String)

class Qualification(val text: String)

class VeaActivity(val startDate : LocalDate, val endDate : Option[LocalDate], val specifiedAreas : List[SpecifiedArea], val qualifications : List[Qualification])

abstract class VeaDetermination(registerId : String, operations : List[VeaOperation], activities : List[VeaActivity])

case class WarlikeDetermination(registerId: String, operations : List[VeaOperation], activities : List[VeaActivity]) extends VeaDetermination(registerId,operations,activities)

case class NonWarlikeDetermination(registerId: String,  operations : List[VeaOperation], activities : List[VeaActivity]) extends VeaDetermination(registerId,operations,activities)

case class HazardousDetermination( registerId: String, operations : List[VeaOperation],  activities : List[VeaActivity]) extends VeaDetermination(registerId,operations,activities)

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
      val n = determinationNode.toString()
      val registerId = (determinationNode \  "federalRegisterOfLegislationReference").text
      val operations = (determinationNode \ "operations" \ "operation").map(o => VeaDeserialisationUtils.OperationFromXml(o)).toList
      val activities = (determinationNode \ "activities" \ "activity" ).map(a => VeaDeserialisationUtils.ActivityFromXml(a)).toList
      (registerId,operations,activities)
    }


    val warlikeDeterminationElements = (node \ "warlikeAndNonWarlike" \ "warlikeDeterminations" \ "determination")
    val warlike: immutable.Seq[WarlikeDetermination] = warlikeDeterminationElements.map(d => getData(d)).map(o => WarlikeDetermination(o._1, o._2, o._3)).toList

    val nonWarlikeDeterminations = (node \ "warlikeAndNonWarlike" \ "non-warlikeDeterminations" \ "determination").map(d => getData(d)).map(o => NonWarlikeDetermination(o._1, o._2, o._3)).toList

    val hazardous = (node \ "hazardous" \ "determinations" \ "determination").map(h => getData(h)).map(o => HazardousDetermination(o._1,o._2,o._3))

    (warlike ++ nonWarlikeDeterminations ++ hazardous).toList
  }

}




