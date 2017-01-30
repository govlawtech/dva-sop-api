
package au.gov.dva.sopapi.tests.parsers;

import java.io.InputStream
import java.time.{LocalDate, OffsetDateTime}

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.sopref.data.Conversions
import au.gov.dva.sopapi.sopref.data.servicedeterminations.StoredServiceDetermination
import au.gov.dva.sopapi.interfaces.model.ServiceType
import au.gov.dva.sopapi.sopref.parsing.ServiceDeterminationsParser
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class ServiceDeterminationParserTests extends FunSuite {


  test("Convert warlike determination to text") {

    val bytes = ParserTestUtils.resourceToBytes("F2016L00994.pdf");
    val result = Conversions.pdfToPlainText(bytes);
    println(result)
    assert(result.size > 0)
  }

  test("Convert non-warlike determination to text") {

    val bytes = ParserTestUtils.resourceToBytes("F2016L00995.pdf");
    val result = Conversions.pdfToPlainText(bytes);
    println(result)
    assert(result.size > 0)
  }

  test("Get register ID from warlike") {

    val text = ParserTestUtils.resourceToString("serviceDeterminations/warlike.txt")
    val result = ServiceDeterminationsParser.getRegisterId(text);
    assert(result == "F2016L00994")

  }

  test("Get register ID from non-warlike") {
    val text = ParserTestUtils.resourceToString("serviceDeterminations/non-warlike.txt")
    val result = ServiceDeterminationsParser.getRegisterId(text)
    assert(result == "F2016L00995")
  }

  test("Get register date from warlike") {
    val text = ParserTestUtils.resourceToString("serviceDeterminations/warlike.txt")
    val result = ServiceDeterminationsParser.getRegisteredDate(text);
    assert(result.get.isEqual(TestUtils.actOdtOf(2016, 6, 6)))
  }

  test("Get register date from non-warlike") {
    val text = ParserTestUtils.resourceToString("serviceDeterminations/non-warlike.txt")
    val result: Option[OffsetDateTime] = ServiceDeterminationsParser.getRegisteredDate(text);
    assert(result.get.isEqual(TestUtils.actOdtOf(2016, 6, 6)))
  }

  test("Get citation from warlike") {
    val text = ParserTestUtils.resourceToString("serviceDeterminations/warlike.txt")
    val result = ServiceDeterminationsParser.getCitation(text);
    assert(result == "Military Rehabilitation and Compensation (Warlike Service) Determination 2016 (No. 1)")
  }

  test("Get citation from non-warlike") {
    val text = ParserTestUtils.resourceToString("serviceDeterminations/non-warlike.txt")
    val result = ServiceDeterminationsParser.getCitation(text);
    assert(result == "Military Rehabilitation and Compensation (Non-warlike Service) Determination 2016 (No. 1)")
  }

  test("Determine service type from warlike citation") {
    val input = "Military Rehabilitation and Compensation (Warlike Service) Determination 2016 (No. 1)"
    val result = ServiceDeterminationsParser.getServiceTypeFromCitation(input)
    assert(result == ServiceType.WARLIKE)
  }

  test("Determine service type from non-warlike citation") {
    val input = "Military Rehabilitation and Compensation (Non-warlike Service) Determination 2016 (No. 1)"
    val result = ServiceDeterminationsParser.getServiceTypeFromCitation(input)
    assert(result == ServiceType.NON_WARLIKE)
  }

  test("Create whole warlike determination") {
    val inputDocx = ParserTestUtils.resourceToBytes("F2016L00994.docx")
    val inputText = ParserTestUtils.resourceToString("serviceDeterminations/warlike.txt")
    val result = ServiceDeterminationsParser.createServiceDetermination(inputDocx, inputText)
    val jsonResult = au.gov.dva.dvasopapi.tests.TestUtils.prettyPrint(StoredServiceDetermination.toJson(result))
    print(jsonResult)
    assert(result != null)

  }



  test("Create whole non-warlike determination") {
    val inputDocx = ParserTestUtils.resourceToBytes("F2016L00995.docx")
    val inputText = ParserTestUtils.resourceToString("serviceDeterminations/non-warlike.txt")
    val result = ServiceDeterminationsParser.createServiceDetermination(inputDocx, inputText)
    val jsonResult = au.gov.dva.dvasopapi.tests.TestUtils.prettyPrint(StoredServiceDetermination.toJson(result))
    print(jsonResult)
    assert(result != null)

  }


}
