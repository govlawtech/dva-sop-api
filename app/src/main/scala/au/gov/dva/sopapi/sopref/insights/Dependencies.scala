package au.gov.dva.SoPPairapi.SoPPairref.insights


import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import au.gov.dva.sopapi.interfaces.model.{Factor, SoPPair}

import scalax.collection.GraphEdge.DiEdge
import collection.JavaConverters._
import scalax.collection.io.dot._
import scalax.collection.Graph
import scalax.collection.edge.LDiEdge


object Dependencies {

  // ideas:
  // make AL with explicit references
  // find factors in common - levenshtein distance, BK tree
  def buildDotString(sopPairs : List[SoPPair]) : String = {
    val dotRoot = DotRootGraph(
      directed = true,
      id = Some(Id(s"SoP Dependencies Graph Generated ${DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())}"))
    )


    def edgeTransformer(innerEdge: Graph[SoPPair,LDiEdge]#EdgeT): Option[(DotGraph,DotEdgeStmt)] = {
       innerEdge.edge match {
         case LDiEdge(source,target,label) => Some(
           (dotRoot,DotEdgeStmt(
             NodeId(source.toString()),
             NodeId(target.toString())
           )))
       }
    }



    val g = buildGraph(sopPairs)
    g.toDot(dotRoot, edgeTransformer)


  }

  private def buildGraph(SoPPairs: List[SoPPair]): Graph[SoPPair, LDiEdge] = {


    val g: Graph[SoPPair, LDiEdge] = Graph.from(SoPPairs,buildEdges(SoPPairs))
    g
  }

  private def buildEdges(SoPPairs: List[SoPPair]) : Seq[LDiEdge[SoPPair]] = {

    def createEdgesForSoPPairRecursive(remainingSoPPairs : List[SoPPair]): Seq[LDiEdge[SoPPair]] = {
      if (remainingSoPPairs.isEmpty) List()
      else createEdgesForSoPPair(remainingSoPPairs.head, SoPPairs.filter(s => s.getConditionName != remainingSoPPairs.head.getConditionName)) ++ createEdgesForSoPPairRecursive(remainingSoPPairs.tail)
    }

    createEdgesForSoPPairRecursive(SoPPairs)
  }


  private def findSoPPairsReferencingConditionInFactors(conditionName: String, sopPairs: List[SoPPair]): List[SoPPair] = {

    def findFactorsContainingConditionName(conditionName: String, SoPPair: SoPPair): List[Factor] = {
      val allFactors: List[Factor] = (SoPPair.getRhSop.getOnsetFactors.asScala
        ++ SoPPair.getRhSop.getAggravationFactors.asScala
        ++ SoPPair.getBopSop.getOnsetFactors.asScala
        ++ SoPPair.getBopSop.getAggravationFactors.asScala) toList

      allFactors.filter(f => f.getText.contains(conditionName))
    }

    sopPairs.filter(SoPPair => findFactorsContainingConditionName(conditionName, SoPPair).nonEmpty)
  }

  private def createEdgesForSoPPair(sopPair : SoPPair, otherSoPPairs : List[SoPPair]): Seq[LDiEdge[SoPPair]] = {
    val sources = findSoPPairsReferencingConditionInFactors(sopPair.getConditionName,otherSoPPairs)
    val target = sopPair
    sources map (s => LDiEdge(s,target)(""))
  }


}


