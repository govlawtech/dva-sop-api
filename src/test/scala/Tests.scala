
import au.gov.dva.sopapi.javatracers.JavaTest
import au.gov.dva.sopapi.scalatracers.ScalaTest
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Tests extends FunSuite {
  test("scala method executes") {
     def library = new ScalaTest()
    assert(library.returnsTrue())
  }

  test("java method executes") {
    assert(JavaTest.returnsTrue())
  }
}


