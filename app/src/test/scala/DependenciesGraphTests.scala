import java.time.OffsetDateTime

import au.gov.dva.sopapi.sopref.SoPs
import au.gov.dva.sopapi.sopref.dependencies.Dependencies
import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import com.google.common.collect.ImmutableSet
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConverters


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

  

}
