import java.time.LocalDate

import au.gov.dva.sopapi.veaops._
import com.google.common.io.Resources
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.xml.{Elem, XML}

@RunWith(classOf[JUnitRunner])
class VeaOperationsTests extends FunSuite {

  test("Deserialise operationXml")
  {
    val testXml =   <operation>
      <name>PROVIDE COMFORT</name>
      <startDate>1991-08-11</startDate>
      <endDate>1996-12-15</endDate>
      <qualifications>
        <qualification>service with the United States elements of the coalition force operation to
          patrol the Iraq No-Fly-Zones
        </qualification>
        <qualification>another qualification</qualification>
      </qualifications>
    </operation>

    val deserialised = VeaDeserialisationUtils.OperationFromXml(testXml)

    assert(deserialised.qualifications.length == 2 && deserialised.specifiedAreas.length == 0)
  }

  test("Deserialise activity")
  {
    val testXml =  <activity>
      <startDate>1973-01-12</startDate>
      <endDate>1975-04-29</endDate>
      <specifiedAreas>
        <specifiedArea>Vietnam (Southern Zone)</specifiedArea>
      </specifiedAreas>

    </activity>

    val result = VeaDeserialisationUtils.ActivityFromXml(testXml)
    assert(result.specifiedAreas.length == 1)
  }

  test("Deserialise everything")
  {
    val root: Elem = XML.load (Resources.getResource("serviceDeterminations/veaServiceReferenceData.xml"))
    val deserialised = VeaDeserialisationUtils.DeterminationsfromXml(root)
    println("Count: " + deserialised.length)
    assert(deserialised.length > 50)

  }

  test ("Correct things at date")
  {
    val testop = new VeaOperation("TESTOP1",LocalDate.of(2017,1,1),Some(LocalDate.of(2018,1,1)),List(),List())
    val testact = new VeaActivity(LocalDate.of(2018,1,1),None,List(),List())
    val testDet1 = new WarlikeDetermination("F0000000",List(testop),List())
    val testDet2 = new NonWarlikeDetermination("F54545454",List(),List(testact))

    val testDets = List(testDet1,testDet2)
    val testDate = LocalDate.of(2017,1,1)
    val result = VeaOperationQueries.getDeterminationsOnDate(testDate,testDets)
    assert(result.values.size == 1)
    assert(result(testDet1).head.asInstanceOf[VeaOperation].name == "TESTOP1")
  }

  test ("Correct things at date 2")
  {
    val testop = new VeaOperation("TESTOP1",LocalDate.of(2017,1,1),Some(LocalDate.of(2018,1,1)),List(),List())
    val testact = new VeaActivity(LocalDate.of(2018,1,1),None,List(),List())
    val testDet1 = new WarlikeDetermination("F0000000",List(testop),List())
    val testDet2 = new NonWarlikeDetermination("F54545454",List(),List(testact))

    val testDets = List(testDet1,testDet2)
    val testDate = LocalDate.of(2018,1,1)
    val result = VeaOperationQueries.getDeterminationsOnDate(testDate,testDets)
    assert(result.values.size == 2)
    assert(result(testDet2).head.asInstanceOf[VeaActivity].endDate.isEmpty)
  }

}
