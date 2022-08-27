import java.time.LocalDate
import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.veaops._
import au.gov.dva.sopapi.veaops.interfaces.VeaOperationalServiceRepository
import com.google.common.collect.ImmutableSet
import com.google.common.io.Resources
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import java.util.Optional
import scala.collection.JavaConverters._
import scala.xml.{Elem, XML}

@RunWith(classOf[JUnitRunner])
class VeaOperationsTests extends FunSuite {


  val testop = new VeaOperation("TESTOP1", LocalDate.of(2017, 1, 1), Some(LocalDate.of(2018, 1, 1)), List(), List(), Set())
  val testact = new VeaActivity("TESTACTIVITY", LocalDate.of(2018, 1, 1), None, List(), List(), Set())

  test("Deserialise operationXml") {
    val testXml = <operation>
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

  test("Deserialise activity") {
    val testXml = <activity>
      <startDate>1973-01-12</startDate>
      <endDate>1975-04-29</endDate>
      <specifiedAreas>
        <specifiedArea>Vietnam (Southern Zone)</specifiedArea>
      </specifiedAreas>

    </activity>

    val result = VeaDeserialisationUtils.ActivityFromXml(testXml)
    assert(result.specifiedAreas.length == 1)
  }

  test("Deserialise everything") {
    val root: Elem = XML.load(Resources.getResource("serviceDeterminations/veaServiceReferenceData.xml"))
    val deserialised = VeaDeserialisationUtils.DeterminationsfromXml(root)
    println("Count: " + deserialised.length)
    assert(deserialised.length > 50)

  }

  test("Correct things at date") {

    val testDet1 = new WarlikeDetermination("F0000000", List(testop), List())
    val testDet2 = new NonWarlikeDetermination("F54545454", List(), List(testact))

    val testDets = List(testDet1, testDet2)
    val testDate = LocalDate.of(2017, 1, 1)
    val result = VeaOperationalServiceQueries.getOpsAndActivitiesOnDate(testDate, testDets)
    assert(result.values.size == 1)
    assert(result(testDet1).head.asInstanceOf[VeaOperation].name == "TESTOP1")
  }

  test("Correct things at date 2") {

    val testDet1 = new WarlikeDetermination("F0000000", List(testop), List())
    val testDet2 = new NonWarlikeDetermination("F54545454", List(), List(testact))

    val testDets = List(testDet1, testDet2)
    val testDate = LocalDate.of(2018, 1, 1)
    val result = VeaOperationalServiceQueries.getOpsAndActivitiesOnDate(testDate, testDets)
    assert(result.values.size == 2)
    assert(result(testDet2).head.asInstanceOf[VeaActivity].endDate.isEmpty)
  }

  test("Correct things at date 3") {
    val testDet1 = new WarlikeDetermination("F0000000", List(testop), List())
    val testDet2 = new NonWarlikeDetermination("F54545454", List(), List(testact))

    val testDets = List(testDet1, testDet2)
    val testDate = LocalDate.of(2018, 1, 2)
    val result = VeaOperationalServiceQueries.getOpsAndActivitiesOnDate(testDate, testDets)
    assert(result.values.size == 1)
    assert(result(testDet2).head.isInstanceOf[VeaActivity])
  }

  test("Correct vea occurances in range") {
    val testDet1 = new WarlikeDetermination("F0000000", List(testop), List())
    val testDet2 = new NonWarlikeDetermination("F54545454", List(), List(testact))
    val testDets = List(testDet1, testDet2)

    val testStartDate = LocalDate.of(2000, 1, 1)
    val testEndDate = LocalDate.of(2018, 1, 1)
    val results = VeaOperationalServiceQueries.getOpsAndActivitiesInRange(testStartDate, Option(testEndDate), testDets)
    assert(results.size == 2)
  }

  test("Correct vea occurances in range: none in interval") {
    val testDet1 = new WarlikeDetermination("F0000000", List(testop), List())
    val testDet2 = new NonWarlikeDetermination("F54545454", List(), List(testact))
    val testDets = List(testDet1, testDet2)

    val testStartDate = LocalDate.of(2000, 1, 1)
    val testEndDate = LocalDate.of(2001, 1, 1)
    val results = VeaOperationalServiceQueries.getOpsAndActivitiesInRange(testStartDate, Option(testEndDate), testDets)
    assert(results.size == 0)
  }

  test("Correct vea occurances in range: one in open ended interval") {
    val testDet1 = new WarlikeDetermination("F0000000", List(testop), List())
    val testDet2 = new NonWarlikeDetermination("F54545454", List(), List(testact))
    val testDets = List(testDet1, testDet2)

    val testStartDate = LocalDate.of(2019, 1, 1)
    val testEndDate = LocalDate.of(2020, 1, 1)
    val results = VeaOperationalServiceQueries.getOpsAndActivitiesInRange(testStartDate, Option(testEndDate), testDets)
    assert(results.size == 1)
  }

  test("Test building json response") {
    val root: Elem = XML.load(Resources.getResource("serviceDeterminations/veaServiceReferenceData.xml"))
    val deserialisedDeterminations: List[VeaDetermination] = VeaDeserialisationUtils.DeterminationsfromXml(root)
    println("det count: " + deserialisedDeterminations.map(d => d.operations.size + d.activities.size).sum)

    val deserialisedPeacekeeping = VeaDeserialisationUtils.PeacekeeepingActivitiesFromXml(root)
    println("peackeeeping count: " + deserialisedPeacekeeping.size)
    val repo = new VeaOperationalServiceRepository {

      override def getPeacekeepingActivities: ImmutableSet[VeaPeacekeepingActivity] = ImmutableSet.copyOf(deserialisedPeacekeeping.asJavaCollection.iterator())

      override def getDeterminations: ImmutableSet[VeaDetermination] = ImmutableSet.copyOf(deserialisedDeterminations.asJavaCollection.iterator())
    }
    val result = Facade.getResponseRangeQuery(LocalDate.of(1888, 1, 1), Optional.of(LocalDate.of(3000, 1, 1)), repo)

    println(TestUtils.prettyPrint(result))

  }

  test("Deserialise peackeeping") {

    val root: Elem = XML.load(Resources.getResource("serviceDeterminations/veaServiceReferenceData.xml"))
    val peacekeeping = VeaDeserialisationUtils.PeacekeeepingActivitiesFromXml(root)
    println(peacekeeping.size)
  }

  test("Deserialise operation with mappings")
  {
    val testData =
        <operation>
          <name>International Security Assistance Force</name>
          <startDate>2003-08-11</startDate>
          <specifiedAreas>
            <specifiedArea>Afghanistan and its superjacent airspace</specifiedArea>
          </specifiedAreas>
          <mappings>
            <mapping regex="ISAF"/>
          </mappings>
        </operation>

    val deserialised = VeaDeserialisationUtils.OperationFromXml(testData)
    assert(deserialised.mappings.head.pattern.pattern() == "ISAF")
  }


  val testRepo = Facade.deserialiseRepository(Resources.toByteArray(Resources.getResource("serviceDeterminations/veaServiceReferenceData.xml")))

  import au.gov.dva.sopapi.veaops.Extensions._

  test("VEA operations correctly classified when operational") {
    // ISAF is warlike, starts on 2003-08-11
    val idOfOpThatIsWarlike = "INTERNATIONAL SECURITY ASSISTANCE FORCE"
    val resultWhenStartDateIsOnstartDateOfOp = testRepo.getOperationalTestResults(idOfOpThatIsWarlike,LocalDate.of(2003,8,11))
    assert(resultWhenStartDateIsOnstartDateOfOp.isOperational)

    val resultWhenStartDateIsBeforeStartDateOfOp = testRepo.getOperationalTestResults(idOfOpThatIsWarlike, LocalDate.of(2003,8,10))
    assert(resultWhenStartDateIsBeforeStartDateOfOp.isOperational)

    val resultWhenTestPeriodEndsBeforeStartDateofOp = testRepo.getOperationalTestResults(idOfOpThatIsWarlike, LocalDate.of(2003,8,9),Some(LocalDate.of(2003,8,10)))
    assert(!resultWhenTestPeriodEndsBeforeStartDateofOp.isOperational)

    val resultWhenTestPeriodEndsOnOpStartDate = testRepo.getOperationalTestResults(idOfOpThatIsWarlike, LocalDate.of(2003,8,9),Some(LocalDate.of(2003,8,11)))
    assert(resultWhenTestPeriodEndsOnOpStartDate.isOperational)
  }

  test("Peacekeeping operations correctly classified") {
    val peackeepingID = "UNMISET"
    // ends day before peackeeping op starts
    val activityEndsBeforePeackeepingStarts = testRepo.getOperationalTestResults(peackeepingID,LocalDate.of(2000,1,1),Some(LocalDate.of(2002,5,19)))
    assert(!activityEndsBeforePeackeepingStarts.isOperational)

    val activityEndsOnDayPeacekeepingStarts = testRepo.getOperationalTestResults(peackeepingID,LocalDate.of(2000,1,1),Some(LocalDate.of(2002,5,20)))
    assert(activityEndsOnDayPeacekeepingStarts.isOperational)
  }

  test("Regex mappings work for determinations")
  {
    val moniker = "ISAF"
    val knownWarlike = testRepo.getOperationalTestResults(moniker,LocalDate.of(2003,8,11))
    assert(knownWarlike.matchingDeterminations.head._1.registerId == "F2014L00151")
    assert(knownWarlike.isOperational)
  }

  test("Regex mappings work for peacekeeeping activities")  {
    val moniker = "UNOMOZ"
    val knownPeacekeeping = testRepo.getOperationalTestResults(moniker,LocalDate.of(1994,10,10))
    assert(knownPeacekeeping.isOperational)
  }

  test("Slipper not matched outside operation period") {
//    <name>SLIPPER</name>
  //    <startDate>2001-10-11</startDate>
   //   <endDate>2009-07-30</endDate>
    val moniker = "OPERATION SLIPPER"
    val result = testRepo.getOperationalTestResults(moniker,LocalDate.of(2009,8,1))
    assert(!result.isOperational)

  }


  test("Slipper IS matched overlapping one day at end of operation period") {
    //    <name>SLIPPER</name>
    //    <startDate>2001-10-11</startDate>
    //   <endDate>2009-07-30</endDate>
    val moniker = "OPERATION SLIPPER"
    val result = testRepo.getOperationalTestResults(moniker, LocalDate.of(2009, 7, 30))
    assert(result.isOperational)

  }



  test("Slipper NOT matched in period that ends before it starts") {
    //    <name>SLIPPER</name>
    //    <startDate>2001-10-11</startDate>
    //   <endDate>2009-07-30</endDate>
    val moniker = "OPERATION SLIPPER"
    val result = testRepo.getOperationalTestResults(moniker, LocalDate.of(2000, 1, 1),Option(LocalDate.of(2000,1,2)))
    assert(!result.isOperational)

  }


}
