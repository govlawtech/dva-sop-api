import java.time.{LocalDate, OffsetDateTime}

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance.{AcceptedConditionDto, AcceptedSequalaeResponseConditionDto, DiagnosedConditionDto, SequelaeRequestDto}
import au.gov.dva.sopapi.interfaces.model.SoPPair
import au.gov.dva.sopapi.sopref.SoPs
import au.gov.dva.sopapi.sopref.dependencies.{AcceptedCondition, Dependencies, DiagnosedCondition}
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


  test("Build graph")
  {
    val result = Dependencies.buildDotStringForAll(sopPairs)
    println(result)

  }

  test("Find cycles")
  {
    val g = Dependencies.buildGraphP(sopPairs)
    val cycles: collection.Set[SoPPair] = Dependencies.findNodesInCycles(g)
    println(cycles)

  }

  test("Test path finding")
  {
    val graph = Dependencies.buildGraphP(sopPairs)
    val diagnosed = sopPairs.asScala.find(sp => sp.getConditionName == "tooth wear").get
    val accepted = sopPairs.asScala.find(sp => sp.getConditionName == "depressive disorder").get
    val result = Dependencies.getShortestPathFromAcceptedToDiagnosed(diagnosed,accepted,graph).map(_.edges)
    println(result)
  }

  test ("Build graph with only accepted and diagnosed"){
    val diagnosed = sopPairs.asScala.find(sp => sp.getConditionName == "bruxism").get
    val accepted = sopPairs.asScala.find(sp => sp.getConditionName == "depressive disorder").get
    val other = sopPairs.asScala.find(sp => sp.getConditionName == "tooth wear").get
    val g: Graph[SoPPair, LDiEdge] = Dependencies.buildGraphP(ImmutableSet.of(diagnosed,accepted,other))
    val sorted = g.topologicalSort
    println(sorted)
  }

  test("Build instant graph") {
    val diagnosedSop = sopPairs.asScala.find(sp => sp.getConditionName == "tooth wear").get
    val acceptedSoP = sopPairs.asScala.find(sp => sp.getConditionName == "depressive disorder").get
    val acceptedSoP2 = sopPairs.asScala.find(sp => sp.getConditionName == "bruxism").get
    val diagnosed = DiagnosedCondition(diagnosedSop, StandardOfProof.ReasonableHypothesis, true, LocalDate.of(2020,1,1))
    val accepted = AcceptedCondition(acceptedSoP, LocalDate.of(2018,1,1))
    val accepted2 = AcceptedCondition(acceptedSoP2,LocalDate.of(2018,2,1))
    val result = Dependencies.getInstantGraph(List(accepted, accepted2),List(diagnosed),false)
    println(result)
  }

  test("Build instant graph with cycles") {
    val diagnosedSop = sopPairs.asScala.find(sp => sp.getConditionName == "depressive disorder").get
    val acceptedSoP = sopPairs.asScala.find(sp => sp.getConditionName == "panic disorder").get
    val diagnosed = DiagnosedCondition(diagnosedSop, StandardOfProof.ReasonableHypothesis, true, LocalDate.of(2020,1,1))
    val accepted = AcceptedCondition(acceptedSoP, LocalDate.of(2018,1,1))
    val result = Dependencies.getInstantGraph(List(accepted),List(diagnosed),false)
    println(Dependencies.toDotString(result))
  }



  test("Should fail to traverse because of time requirement") {
    val diagnosedSop = sopPairs.asScala.find(sp => sp.getConditionName == "personality disorder").get
    val acceptedSoP = sopPairs.asScala.find(sp => sp.getConditionName == "substance use disorder").get
    // five years time limit
    val diagnosed = DiagnosedCondition(diagnosedSop, StandardOfProof.ReasonableHypothesis, true, LocalDate.of(2017,1,1))
    val accepted = AcceptedCondition(acceptedSoP, LocalDate.of(2011,12,31))
    val result = Dependencies.getInstantGraph(List(accepted),List(diagnosed),true)
    println(result)
  }

  test("Sprain detected") {
    val acceptedSoP = sopPairs.asScala.find(sp => sp.getConditionName == "sprain and strain").get
    val diagnosedSop = sopPairs.asScala.find(sp => sp.getConditionName == "internal derangement of the knee").get
    // five years time limit
    val diagnosed = DiagnosedCondition(diagnosedSop, StandardOfProof.ReasonableHypothesis, true, LocalDate.of(2017,1,1))
    val accepted = AcceptedCondition(acceptedSoP, LocalDate.of(2016,12,31))
    val result = Dependencies.getInstantGraph(List(accepted),List(diagnosed),false)
    println(result)
  }

  test("Integration test of response generation") {
    val testRequest = new SequelaeRequestDto(
      List(
        new AcceptedConditionDto("depressive disorder","M000",LocalDate.of(2019,10,23))

      ).asJava,
      List(
        new DiagnosedConditionDto("bruxism","M0000",LocalDate.of(2019,10,24),StandardOfProof.ReasonableHypothesis,true),
        new DiagnosedConditionDto("tooth wear","M0000",LocalDate.of(2019,10,25),StandardOfProof.ReasonableHypothesis,true),
        new DiagnosedConditionDto("panic disorder","M0000", LocalDate.of(2019,10,26),StandardOfProof.ReasonableHypothesis,true),
        new DiagnosedConditionDto("irritable bowel syndrome","M0000", LocalDate.of(2019,10,27),StandardOfProof.ReasonableHypothesis,true)
      ).asJava
    )

    val diagnosed = sopPairs.asScala.find(sp => sp.getConditionName == "bruxism").get
    val accepted = sopPairs.asScala.find(sp => sp.getConditionName == "depressive disorder").get
    val other = sopPairs.asScala.find(sp => sp.getConditionName == "tooth wear").get
    val pd = sopPairs.asScala.find(sp => sp.getConditionName == "panic disorder").get
    val ibs = sopPairs.asScala.find(sp => sp.getConditionName == "irritable bowel syndrome").get
    val sps = ImmutableSet.of(diagnosed,accepted,other,pd,ibs);

    val result = Dependencies.getInferredSequelae(testRequest,sps)
    println(result.toJsonString)
  }

}
