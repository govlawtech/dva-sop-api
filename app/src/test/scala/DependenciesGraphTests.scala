import java.time.{LocalDate, OffsetDateTime}

import au.gov.dva.sopapi.dtos.StandardOfProof
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
    val result = Dependencies.buildDotString(sopPairs, ImmutableSet.of("depressive disorder"))
    println(result)

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
    val sorted = Dependencies.topoSort(g)
    println(sorted)
  }

  test("Build instant graph") {
    val diagnosedSop = sopPairs.asScala.find(sp => sp.getConditionName == "tooth wear").get
    val acceptedSoP = sopPairs.asScala.find(sp => sp.getConditionName == "depressive disorder").get
    val acceptedSoP2 = sopPairs.asScala.find(sp => sp.getConditionName == "bruxism").get
    val diagnosed = DiagnosedCondition(diagnosedSop, StandardOfProof.ReasonableHypothesis, true, LocalDate.of(2020,1,1))
    val accepted = AcceptedCondition(acceptedSoP, StandardOfProof.ReasonableHypothesis, acceptedSoP.getRhSop.getOnsetFactors.get(0),LocalDate.of(2018,1,1))
    val accepted2 = AcceptedCondition(acceptedSoP2,StandardOfProof.ReasonableHypothesis,acceptedSoP2.getRhSop.getOnsetFactors.get(0),LocalDate.of(2018,2,1))
    val result = Dependencies.getInstantGraph(List(accepted, accepted2),List(diagnosed))
    println(result)

  }





}
