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
import scalax.collection.Graph
import scalax.collection.GraphTraversal.{Predecessors, Successors}
import scalax.collection.edge.LDiEdge

case class FactorRef(dependentSop: SoP, linkingFactor: Factor, targetSoPPair : SoPPair) {
    private def isOnset = dependentSop.getOnsetFactors.asScala.exists(f => f.getParagraph == linkingFactor.getParagraph)
    private def getStandardOfProof = dependentSop.getStandardOfProof.toAbbreviatedString
    private def getVariant = linkingFactor.getConditionVariant
    def getLabel : String = {
      val onsetLabel = if (isOnset) "onset" else "clinical worsening";
      val para = linkingFactor.getParagraph
      val variantLabel = if (getVariant.isPresent) s",${getVariant.get().getName}" else ""
      s"($para: $onsetLabel,$getStandardOfProof$variantLabel)"
    }

  override def toString: String = getLabel
}





object Dependencies {

  // ideas:
  // make AL with explicit references
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

  def getChildrenOf(conditionName : String, conditions: ImmutableSet[SoPPair]) : String= {
    val g = buildGraph(conditions.asScala.toList)
    val sopPair = conditions.asScala.find(c => c.getConditionName == conditionName)
    if (sopPair.isEmpty) return ""
    val conditionNode: Option[g.NodeT] = g.find(sopPair.get)
    val children = conditionNode.get.incoming.map(e => e.edge.sources.head.getConditionName)
    children.toList.mkString(Properties.lineSeparator)
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

  private def findFactorReferences(dependentSoPPair : SoPPair, otherSoPPairs : List[SoPPair]) : Map[SoPPair, List[FactorRef]] = {
    def definedTermsContainsPhrase(conditionName: String,  factor: Factor) = {
      val definitions = factor.getDefinedTerms.asScala
      definitions.exists(d => d.getDefinition.contains(conditionName))
    }

    def gatherFactorRefs(dependentSop: SoP, targetSopPair: SoPPair) : List[FactorRef] = {
      def getFactorsReferencesingCondition(factors: List[Factor] ) = factors.filter(f => f.getText.contains(target  SopPair.getConditionName) || definedTermsContainsPhrase(targetSopPair.getConditionName,f))
      def toFactorRef(f : Factor) = FactorRef(dependentSop,f,targetSopPair)
      val onsetFactorsReferencingCondition = getFactorsReferencesingCondition(dependentSop.getOnsetFactors.asScala.toList)
      val aggFactorsReferencingCondition = getFactorsReferencesingCondition(dependentSop.getAggravationFactors.asScala.toList)
      (onsetFactorsReferencingCondition ++ aggFactorsReferencingCondition).map(toFactorRef(_))
    }

    otherSoPPairs.flatMap(

    )
  }


  private def createEdgesForSoPPair(sopPair : SoPPair, otherSoPPairs : List[SoPPair]): Seq[LDiEdge[SoPPair]] = {
    val factorReferencesFromDependentSop:  List[FactorRef] = findFactorReferences(sopPair,otherSoPPairs)
    val groupedByTarget: Map[SoPPair, List[FactorRef]] = factorReferencesFromDependentSop.groupBy(f => f.targetSoPPair)
    groupedByTarget.map(fr => LDiEdge())
    factorReferencesFromDependentSop.map(fr => LDiEdge(sopPair,fr.)(fr._2))
  }

  private def liftSubGraphForCondition(graph : Graph[SoPPair, LDiEdge], condition : SoPPair): Graph[SoPPair,LDiEdge] = {
    val root = graph get condition
    val subGraph = root.innerNodeTraverser.withDirection(Predecessors).withMaxDepth(1).toGraph
    subGraph
  }
}

