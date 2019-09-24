package au.gov.dva.sopapi.sopref.dependencies

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import au.gov.dva.sopapi.interfaces.model.{Factor, SoP, SoPPair}
import com.google.common.collect.{ImmutableList, ImmutableSet}
import org.w3c.dom.traversal.NodeFilter
import scalax.collection.GraphEdge.DiEdge

import collection.JavaConverters._
import scala.util.Properties
import scalax.collection.io.dot._
import scalax.collection.{Graph, GraphEdge}
import scalax.collection.GraphTraversal.{Predecessors, Successors}
import scalax.collection.edge.LDiEdge

import scala.collection.immutable

case class FactorRef(linkingFactor: Factor) {

    override def toString: String =  {
      if (linkingFactor.getConditionVariant.isPresent)
        s"${linkingFactor.getParagraph} (${linkingFactor.getConditionVariant.get().getName})"
          else s"${linkingFactor.getParagraph}"
      }

}

case class FactorRefForSoPPair(dependentSoPPair: SoPPair, targetSoPPair: SoPPair, rhRefs: List[FactorRef], bopRefs: List[FactorRef]) {
  override def toString = if (rhRefs != bopRefs) s"RH: ${rhRefs.mkString(", ")}; BoP: ${bopRefs.mkString(", ")}" else s"${rhRefs.mkString(", ")}"
}


object Dependencies {

  // ideas:
  // find factors in common - levenshtein distance, BK tree
  def buildDotString(sopPairs : ImmutableSet[SoPPair], conditionWhitelist: ImmutableSet[String]) : String = {
    val dotRoot = DotRootGraph(
      directed = true,
      id = Some(Id(s"SoP Dependencies Graph Generated ${DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())}"))
    )

    def edgeTransformer(innerEdge: Graph[SoPPair,LDiEdge]#EdgeT): Option[(DotGraph,DotEdgeStmt)] = {
      innerEdge.edge match {
        case LDiEdge(source,target,label) => Some(
          (dotRoot,DotEdgeStmt(
            NodeId(source.toString()),
            NodeId(target.toString()),
            Seq(DotAttr(Id("label"),Id(label.toString)))
          )))
      }
    }
    val g = buildGraph(sopPairs.asScala.toList)
    val testCondition = sopPairs.asScala.filter(sp => conditionWhitelist.asScala.contains(sp.getConditionName)).head
    val subGraph = liftSubGraphForCondition(g,testCondition)
    subGraph.toDot(dotRoot, edgeTransformer)
  }

  private def buildGraph(SoPPairs: List[SoPPair]): Graph[SoPPair, LDiEdge] = {
    val edges = buildEdges(SoPPairs)
    val g: Graph[SoPPair, LDiEdge] = Graph.from(SoPPairs,edges)
    g
  }

  private def buildEdges(SoPPairs: List[SoPPair]) : Seq[LDiEdge[SoPPair]] = {
    def createEdgesForSoPPairRecursive(remainingSoPPairs : List[SoPPair]): Seq[LDiEdge[SoPPair]] = {
      if (remainingSoPPairs.isEmpty) List()
      else createEdgesForSoPPair(remainingSoPPairs.head, SoPPairs.filter(s => s.getConditionName != remainingSoPPairs.head.getConditionName)) ++ createEdgesForSoPPairRecursive(remainingSoPPairs.tail)
    }
    createEdgesForSoPPairRecursive(SoPPairs)
  }

  private def findFactorReferences(dependentSoPPair : SoPPair, otherSoPPairs : List[SoPPair]) : List[FactorRefForSoPPair] = {
    def definedTermsContainsPhrase(conditionName: String,  factor: Factor) = {
      val definitions = factor.getDefinedTerms.asScala
      definitions.exists(d => d.getDefinition.contains(conditionName))
    }

    def gatherFactorRefs(dependentSop: SoP, targetSopPair: SoPPair) : List[FactorRef] = {
      def getFactorsReferencesingCondition(factors: List[Factor] ) = factors.filter(f => f.getText.contains(targetSopPair.getConditionName) || definedTermsContainsPhrase(targetSopPair.getConditionName,f))
      def toFactorRef(f : Factor) = FactorRef(f)
      val onsetFactorsReferencingCondition = getFactorsReferencesingCondition(dependentSop.getOnsetFactors.asScala.toList)
      val aggFactorsReferencingCondition = getFactorsReferencesingCondition(dependentSop.getAggravationFactors.asScala.toList)
      (onsetFactorsReferencingCondition ++ aggFactorsReferencingCondition).map(toFactorRef(_))
    }

    def getFactorRefForSopPair(dependentSoPPair : SoPPair, targetSoPPair : SoPPair) : FactorRefForSoPPair = {
      val rhRefs = gatherFactorRefs(dependentSoPPair.getRhSop,targetSoPPair)
      val bopRefs = gatherFactorRefs(dependentSoPPair.getRhSop,targetSoPPair)
      FactorRefForSoPPair(dependentSoPPair, targetSoPPair, rhRefs,bopRefs)
    }

    otherSoPPairs.map(ts => getFactorRefForSopPair(dependentSoPPair,ts))
  }

  private def createEdgesForSoPPair(sopPair : SoPPair, otherSoPPairs : List[SoPPair]): immutable.Seq[LDiEdge[SoPPair] with GraphEdge.EdgeCopy[LDiEdge] {
    type L1 = FactorRefForSoPPair
  }] = {
    val factorReferencesFromDependentSop = findFactorReferences(sopPair,otherSoPPairs).filter(fr => !fr.bopRefs.isEmpty || !fr.rhRefs.isEmpty)
    factorReferencesFromDependentSop.map(fr => LDiEdge(fr.dependentSoPPair,fr.targetSoPPair)(fr))
  }

  private def liftSubGraphForCondition(graph : Graph[SoPPair, LDiEdge], condition : SoPPair): Graph[SoPPair,LDiEdge] = {
    val root = graph get condition
    val subGraph = root.innerNodeTraverser.withDirection(Predecessors).withMaxDepth(1).toGraph
    subGraph
  }
}

