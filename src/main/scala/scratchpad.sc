
import scala.util.parsing.combinator.RegexParsers

class TestParser extends RegexParsers {

  def paraParser : Parser[String] = """\([a-z]+\)""".r


}


val parser = new TestParser
val result = parser.parseAll(parser.paraParser,"(a)")

