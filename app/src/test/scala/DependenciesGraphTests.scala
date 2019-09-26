import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner



@RunWith(classOf[JUnitRunner])
class DependenciesGraphTests extends  FunSuite {

  test("Load stored sops for graph test") {

    val sops = ParserTestUtils.getAllSopsInDir("sopsForGraphTest")
    sops.foreach(ss => println(ss.getConditionName))
  }

  

}
