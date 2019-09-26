import java.time.OffsetDateTime

import au.gov.dva.sopapi.sopref.SoPs
import au.gov.dva.sopapi.sopref.dependencies.Dependencies
import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import com.google.common.collect.ImmutableSet
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
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
    val g = Dependencies.buildGraphP(ImmutableSet.of(diagnosed,accepted,other))
    println(g)

  }



}
