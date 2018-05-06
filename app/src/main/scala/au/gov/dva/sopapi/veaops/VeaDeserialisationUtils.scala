package au.gov.dva.sopapi.veaops

import java.time.LocalDate
import java.time.format.DateTimeFormatter


import scala.collection.immutable
import scala.util.matching.Regex

object VeaDeserialisationUtils {

  def specifiedAreas(specifiedAreasNode: scala.xml.Node): List[SpecifiedArea] =
    (specifiedAreasNode \ "specifiedAreas" \ "specifiedArea").map(i => new SpecifiedArea(scala.xml.Utility.trim(i).text)).toList

  def qualifications(qualificationsNode: scala.xml.Node): List[Qualification] =
    (qualificationsNode \ "qualifications" \ "qualification").map(i => new Qualification(scala.xml.Utility.trim(i).text)).toList

  def OperationFromXml(node: scala.xml.Node): VeaOperation = {
    val name = (node \ "name").text
    val startDate = LocalDate.parse((node \ "startDate").text, DateTimeFormatter.ISO_LOCAL_DATE)
    val endDate = (node \ "endDate").headOption.map(i => LocalDate.parse(i.text, DateTimeFormatter.ISO_LOCAL_DATE))
    val specifiedAreas = VeaDeserialisationUtils.specifiedAreas(node)
    val qualifications = VeaDeserialisationUtils.qualifications(node)
    val mappings = GetMappings(node)
    new VeaOperation(name, startDate, endDate, specifiedAreas, qualifications, mappings)
  }

  def ActivityFromXml(node: scala.xml.Node): VeaActivity = {

    val shortName = (node \ "shortName").text
    val startDate = LocalDate.parse((node \ "startDate").text, DateTimeFormatter.ISO_LOCAL_DATE)
    val endDate = (node \ "endDate").headOption.map(i => LocalDate.parse(i.text, DateTimeFormatter.ISO_LOCAL_DATE))
    val specifiedAreas = VeaDeserialisationUtils.specifiedAreas(node)
    val qualifications = VeaDeserialisationUtils.qualifications(node)
    val mappings = GetMappings(node)
    new VeaActivity(shortName, startDate, endDate, specifiedAreas, qualifications,mappings)
  }


  def DeterminationsfromXml(node: scala.xml.Node): List[VeaDetermination] = {
    def getData(determinationNode: scala.xml.Node) = {
      val registerId = (determinationNode \ "federalRegisterOfLegislationReference").text
      val operations = (determinationNode \ "operations" \ "operation").map(o => VeaDeserialisationUtils.OperationFromXml(o)).toList
      val activities = (determinationNode \ "activities" \ "activity").map(a => VeaDeserialisationUtils.ActivityFromXml(a)).toList
      (registerId, operations, activities)
    }

    assert(node.label == "veaServiceReferenceData")
    val warlikeDeterminationElements = node \ "warlikeAndNonWarlike" \ "warlikeDeterminations" \ "determination"
    val warlike: immutable.Seq[WarlikeDetermination] = warlikeDeterminationElements.map(d => getData(d)).map(o => WarlikeDetermination(o._1, o._2, o._3)).toList

    val nonWarlikeDeterminations = (node \ "warlikeAndNonWarlike" \ "non-warlikeDeterminations" \ "determination").map(d => getData(d)).map(o => NonWarlikeDetermination(o._1, o._2, o._3)).toList

    val hazardous = (node \ "hazardous" \ "determinations" \ "determination").map(h => getData(h)).map(o => HazardousDetermination(o._1, o._2, o._3))

    (warlike ++ nonWarlikeDeterminations ++ hazardous).toList
  }


  private def GetMappings(node : scala.xml.Node) : Set[Regex] = {
    (node \ "mappings"  \ "mapping").map(n => new Regex(n.text)).toSet
  }

  def PeacekeeepingActivitiesFromXml(node: scala.xml.Node) : List[VeaPeacekeepingActivity] = {
    assert(node.label == "veaServiceReferenceData")



    def noticeToActivity(node: scala.xml.Node) = {
      assert(node.label == "notice")
      val shortName = (node \ "shortName").text
      val desc = (node \ "descriptionOfPeacekeepingForce").text
      val startDate = LocalDate.parse((node \ "initialDateAsAPeaceKeepingForce").text,DateTimeFormatter.ISO_LOCAL_DATE)
      val mappings = GetMappings(node)
      val legalSource = (node \ "legalSource").text
      VeaPeacekeepingActivity(shortName,desc,startDate,legalSource,mappings)
    }

    def itemToActivity(node: scala.xml.Node) = {
      assert(node.label == "item")
      val number = (node \ "number").text
      val desc = (node \ "descriptionOfPeacekeepingForce").text
      val startDate = LocalDate.parse((node \ "initialDateAsAPeaceKeepingForce").text,DateTimeFormatter.ISO_LOCAL_DATE)
      val mappings = GetMappings(node)
      val legalSource =  s"Veterans Entitlements' Act 1998 Sch 3, item $number"
      VeaPeacekeepingActivity(desc,desc,startDate,legalSource,mappings)
    }


    val sch3Activities = (node \ "peacekeeping" \ "veaSch3" \ "items" \ "item").map(n => itemToActivity(scala.xml.Utility.trim(n)))

    val noticeActivities = (node \ "peacekeeping"  \ "notices" \ "notice").map(n => noticeToActivity(scala.xml.Utility.trim(n)))

    (sch3Activities ++ noticeActivities).sortBy(i => i.initialDate.toEpochDay).toList

  }

}
