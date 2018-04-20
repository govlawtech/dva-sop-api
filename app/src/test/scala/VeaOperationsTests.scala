import java.time.LocalDate

import au.gov.dva.sopapi.veaops.{VeaActivity, VeaDeserialisationUtils, VeaOperation, VeaOperationQueries}
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
    val root: Elem = XML.load (Resources.getResource("serviceDeterminations/veaServiceReferenceData.xml"))
    val data = VeaDeserialisationUtils.DeterminationsfromXml(root)
    val testDate = LocalDate.of(2017,1,1)
    val results = VeaOperationQueries.getDeterminationsOnDate(testDate,data)
    for (i <- results) {
      println(i)
    }

  }
}
