import java.io.ByteArrayOutputStream
import java.time.{LocalDate, OffsetDateTime}

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance.{AcceptedConditionDto, AcceptedSequalaeResponseConditionDto, DiagnosedConditionDto, SequelaeRequestDto}
import au.gov.dva.sopapi.interfaces.model.SoPPair
import au.gov.dva.sopapi.sopref.SoPs
import au.gov.dva.sopapi.sopref.dependencies._
import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import com.google.common.collect.ImmutableSet
import com.sun.xml.bind.api.impl.NameConverter.Standard
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scalax.collection.Graph
import scalax.collection.edge.LDiEdge

import collection.JavaConverters._


@RunWith(classOf[JUnitRunner])
class DependenciesGraphTests extends  FunSuite {

  val sops = ParserTestUtils.getAllSopsInDir("sopsForGraphTest").toArray
  val sopPairs = SoPs.groupSopsToPairs(ImmutableSet.copyOf(sops), OffsetDateTime.now())
  test("Load stored sops for graph test") {

    val sops = ParserTestUtils.getAllSopsInDir("sopsForGraphTest")
    sops.foreach(ss => println(ss.getConditionName))
  }



  test("Build instant graph") {
    val diagnosedSop = sopPairs.asScala.find(sp => sp.getConditionName == "tooth wear").get
    val acceptedSoP = sopPairs.asScala.find(sp => sp.getConditionName == "depressive disorder").get
    val acceptedSoP2 = sopPairs.asScala.find(sp => sp.getConditionName == "bruxism").get
    val diagnosed = DiagnosedCondition(diagnosedSop,  true, LocalDate.of(2020, 1, 1))
    val accepted = AcceptedCondition(acceptedSoP, StandardOfProof.ReasonableHypothesis,  LocalDate.of(2018, 1, 1))
    val accepted2 = AcceptedCondition(acceptedSoP2, StandardOfProof.ReasonableHypothesis,  LocalDate.of(2018, 2, 1))
    val result = Dependencies.getInstantGraph(List(accepted, accepted2), List(diagnosed), false)
    println(result)
  }

  test("Build instant graph with cycles") {
    val diagnosedSop = sopPairs.asScala.find(sp => sp.getConditionName == "depressive disorder").get
    val acceptedSoP = sopPairs.asScala.find(sp => sp.getConditionName == "panic disorder").get
    val diagnosed = DiagnosedCondition(diagnosedSop,  true, LocalDate.of(2020, 1, 1))
    val accepted = AcceptedCondition(acceptedSoP, StandardOfProof.ReasonableHypothesis, LocalDate.of(2018, 1, 1))
    val result = Dependencies.getInstantGraph(List(accepted), List(diagnosed), false)
    println(Dependencies.toDotString(result))
  }


  test("Should fail to traverse because of time requirement") {
    val diagnosedSop = sopPairs.asScala.find(sp => sp.getConditionName == "personality disorder").get
    val acceptedSoP = sopPairs.asScala.find(sp => sp.getConditionName == "substance use disorder").get
    // five years time limit
    val diagnosed = DiagnosedCondition(diagnosedSop,  true, LocalDate.of(2017, 1, 1))
    val accepted = AcceptedCondition(acceptedSoP, StandardOfProof.ReasonableHypothesis, LocalDate.of(2011, 12, 31))
    val result = Dependencies.getInstantGraph(List(accepted), List(diagnosed), true)
    println(result)
  }

  test("Sprain detected") {
    val acceptedSoP = sopPairs.asScala.find(sp => sp.getConditionName == "sprain and strain").get
    val diagnosedSop = sopPairs.asScala.find(sp => sp.getConditionName == "internal derangement of the knee").get
    // five years time limit
    val diagnosed = DiagnosedCondition(diagnosedSop, true, LocalDate.of(2017, 1, 1))
    val accepted = AcceptedCondition(acceptedSoP, StandardOfProof.ReasonableHypothesis, LocalDate.of(2016, 12, 31))
    val result = Dependencies.getInstantGraph(List(accepted), List(diagnosed), false)
    println(result)
  }

  test("Integration test of response generation") {
    val testRequest = new SequelaeRequestDto(
      List(
        new AcceptedConditionDto("depressive disorder", StandardOfProof.ReasonableHypothesis, null, null, LocalDate.of(2019, 10, 23))

      ).asJava,
      List(
        new DiagnosedConditionDto("bruxism", null, null, LocalDate.of(2019, 10, 24), StandardOfProof.ReasonableHypothesis, true),
        new DiagnosedConditionDto("tooth wear", null, null, LocalDate.of(2019, 10, 25), StandardOfProof.ReasonableHypothesis, true),
        new DiagnosedConditionDto("panic disorder", null, null, LocalDate.of(2019, 10, 26), StandardOfProof.ReasonableHypothesis, true),
        new DiagnosedConditionDto("irritable bowel syndrome", null, null, LocalDate.of(2019, 10, 27), StandardOfProof.ReasonableHypothesis, true)
      ).asJava
    )

    val diagnosed = sopPairs.asScala.find(sp => sp.getConditionName == "bruxism").get
    val accepted = sopPairs.asScala.find(sp => sp.getConditionName == "depressive disorder").get
    val other = sopPairs.asScala.find(sp => sp.getConditionName == "tooth wear").get
    val pd = sopPairs.asScala.find(sp => sp.getConditionName == "panic disorder").get
    val ibs = sopPairs.asScala.find(sp => sp.getConditionName == "irritable bowel syndrome").get
    val sps = ImmutableSet.of(diagnosed, accepted, other, pd, ibs);

    val result = Dependencies.getInferredSequelae(testRequest, sps)
    println(result.toJsonString)
  }


  test("Test deserialisation of request")
   {
     val exampleRequest = ParserTestUtils.resourceToString("sequelae/exampleRequest.json")
     val result = SequelaeRequestDto.fromJsonString(exampleRequest)
     println(result)
   }

  test("Render graphviz image in process")
  {
    val os = new ByteArrayOutputStream()
    val result: Unit = DotToImage.render("digraph \"SoP Dependencies Graph Generated 2019-11-06T07:40:13.127+11:00\" {\n\t\"panic disorder\" -> \"depressive disorder\" [label = \"RH: 9(11), 9(23); BoP: 9(11), 9(22)\"]\n\t\"depressive disorder\" -> \"panic disorder\" [label = \"RH: 9(1) (major depressive disorder, major depressive episode, persistent depressive disorder,\npremenstrual dysphoric disorder, other specified depressive disorder and unspecified depressive\ndisorder); BoP: 9(1) (major depressive disorder, major depressive episode, persistent depressive\ndisorder, premenstrual dysphoric disorder, other specified depressive disorder and unspecified\ndepressive disorder)\"]\n}",os)
    println(os.toByteArray)
  }

  test("Test traversal with standard of proof ") {
    val dd: SoPPair = sopPairs.asScala.find(sp => sp.getConditionName == "depressive disorder").get
    val bruxism = sopPairs.asScala.find(sp => sp.getConditionName == "bruxism").get
    val toothWear = sopPairs.asScala.find(sp => sp.getConditionName == "tooth wear").get
    val accepted = AcceptedCondition(dd, StandardOfProof.ReasonableHypothesis,  LocalDate.of(2018, 1, 1))
    val diagnosedBruxism = DiagnosedCondition(bruxism,true,LocalDate.of(2018,2,1))
    val diagnosedToothWear = DiagnosedCondition(toothWear,true,LocalDate.of(2018,3,1))


    val graph = Dependencies.getInstantGraph(List(accepted), List(diagnosedBruxism,diagnosedToothWear), false)

    val paths = Dependencies.getPaths(graph,Set(accepted),Set(diagnosedBruxism,diagnosedToothWear))

    println(paths)

    val prettyPrinted = Dependencies.getPrettyPrintedReasonsForSequela(paths.head,Set(accepted),Set(diagnosedBruxism,diagnosedToothWear),Configuration.shouldAccept)
    println(prettyPrinted)

  }

}
