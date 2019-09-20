package au.gov.dva.sopapi.tests.parsertests.subfactors

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import au.gov.dva.dvasopapi.tests.TestUtils
import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, Factor, SoP, SoPPair}
import au.gov.dva.sopapi.sopref.data.sops.StoredSop
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, SubFactorInfo}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.paragraphReferenceSplitters.NewSoPStyleParaReferenceSplitter
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.subfactors.{NewStyleSubFactorParser, OldStyleSubfactorParser}
import au.gov.dva.sopapi.tests.parsers.ParserTestUtils
import com.google.common.collect.ImmutableSet
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import scalax.collection.GraphEdge.DiEdge
import scalax.collection._
import scalax.collection.edge.WDiEdge
import scalax.collection.edge.Implicits._
import scalax.collection.io.dot._
import scalax.collection.Graph
import scalax.collection.edge.LDiEdge
import scalax.collection.immutable.DefaultGraphImpl

import scala.collection.JavaConverters._

@RunWith(classOf[JUnitRunner])
class DependenciesTests extends FunSuite {




  test("Figure out subgraphs"){

    val g = Graph[String, LDiEdge](("A"~+>"B")("label"),("B"~+>"C")("label2"))

    val dotRoot = DotRootGraph(
      directed = true,
      id = Some(Id(s"SoP Dependencies Graph Generated ${DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())}"))
    )

    def edgeTransformer(innerEdge: Graph[String,LDiEdge]#EdgeT): Option[(DotGraph,DotEdgeStmt)] = {
      innerEdge.edge match {
        case LDiEdge(source,target,label) => Some(
          (dotRoot,DotEdgeStmt(
            NodeId(source.toString()),
            NodeId(target.toString()),
            List(DotAttr(Id("label"), Id(label.toString))

          ))))
      }
    }

    val asDot = g.toDot(dotRoot,edgeTransformer)
    println(asDot)

    val bNode = g get "B"
    println(bNode)

    val subGraph = bNode.innerNodeTraverser.toGraph

    val subGraphAsDot = subGraph.toDot(dotRoot,edgeTransformer)


    println(subGraphAsDot)




  }

}
