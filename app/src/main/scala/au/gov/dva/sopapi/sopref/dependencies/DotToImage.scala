package au.gov.dva.sopapi.sopref.dependencies
import java.io.OutputStream
import java.nio.charset.{Charset, StandardCharsets}

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.model.Factory._
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.parse.Parser


object DotToImage {

    def render(dotString: String, outputStream: OutputStream) = {
        def hackInLinks(dotString : String) = {
          dotString
            .replaceAll("""RH:\s+([A-Z0-9]{11,11})""","""<a href="https://legislation.gov.au/Details/$1">RH: $1</a>""")
            .replaceAll("""BoP:\s+([A-Z0-9]{11,11})""","""<a href="https://legislation.gov.au/Details/$1">BoP: $1</a>""")
        }
       val graph = Parser.read(dotString)
       val svgString = Graphviz.fromGraph(graph).render(Format.SVG).toString
       val svgStringWithLinksHackedIn = hackInLinks(svgString)
       val bytes = svgStringWithLinksHackedIn.getBytes(StandardCharsets.UTF_8)
       outputStream.write(bytes)

       //Graphviz.fromGraph(graph).width(200).render(Format.SVG).toOutputStream(outputStream)

    }

}
