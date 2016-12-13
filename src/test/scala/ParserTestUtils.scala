import java.io.InputStream

import scala.io.Source

object ParserTestUtils {
  def resourceToBytes(resourcePath : String) = {
    val sourceResourceStream: InputStream = getClass().getResourceAsStream(resourcePath);
    Stream.continually(sourceResourceStream.read).takeWhile(_ != -1).map(_.toByte).toArray;

  }

  def resourceToString(resourcePath : String) = {
    Source.fromInputStream(getClass.getResourceAsStream(resourcePath)).mkString;
  }
}
