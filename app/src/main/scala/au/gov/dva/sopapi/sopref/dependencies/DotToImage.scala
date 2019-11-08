package au.gov.dva.sopapi.sopref.dependencies
import java.io.OutputStream

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.model.Factory._
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.parse.Parser


object DotToImage {

    def render(dotString: String, outputStream: OutputStream) = {
        def hackInLinks(dotString : String) = {
          dotString.replaceAll("RH: ([0-9A-Z){11,11})","<a href='https://legislation.gov.au/Details/$1'>$1</a>")
        }

       val graph = Parser.read(hackInLinks(dotString))


       Graphviz.fromGraph(graph).width(200).render(Format.SVG).toOutputStream(outputStream)

    }

}
