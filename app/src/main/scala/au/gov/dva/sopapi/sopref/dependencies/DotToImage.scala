package au.gov.dva.sopapi.sopref.dependencies
import java.io.OutputStream

import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.model.Factory._
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.parse.Parser


object DotToImage {

    def render(dotString: String, outputStream: OutputStream) = {
       val graph = Parser.read(dotString)

       Graphviz.fromGraph(graph).width(200).render(Format.SVG).toOutputStream(outputStream)

    }

}
