package au.gov.dva.sopapi.sopref.dependencies

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

import au.gov.dva.sopapi.interfaces.model.{Factor, SoP, SoPPair}
import com.google.common.collect.{ImmutableList, ImmutableSet}
import scalax.collection.GraphEdge.DiEdge

import collection.JavaConverters._
import scala.util.Properties
import scalax.collection.io.dot._
import scalax.collection.Graph
import scalax.collection.edge.LDiEdge

abstract class FactorRef
case class FactorConditionReference(dependentSop: SoP, dependentSopFactor: Factor, targetCondition: String) extends FactorRef
case class FactorConditionReferenceWithTime(dependentSop: SoP, dependentSopFactor: Factor, targetCondition: String, days: Int) extends FactorRef

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
            NodeId(target.toString())
          )))
      }
    }

    val g = buildGraph(sopPairs.asScala.toList)

    val testCondition = sopPairs.asScala.filter(sp => conditionWhitelist.asScala.contains(sp.getConditionName)).head

    

    g.toDot(dotRoot, edgeTransformer)
  }

  def getChildrenOf(conditionName : String, conditions: ImmutableSet[SoPPair]) : String= {
    val g = buildGraph(conditions.asScala.toList)
    val sopPair = conditions.asScala.find(c => c.getConditionName == conditionName)
    if (sopPair.isEmpty) return ""
    val conditionNode: Option[g.NodeT] = g.find(sopPair.get)
    val children = conditionNode.get.incoming.map(e => e.edge.sources.head.getConditionName)
    children.toList.mkString(Properties.lineSeparator)
  }

  def getSopsMentioningPhraseInFactors(phrase : String, sopPairs: ImmutableSet[SoPPair]) : String = {
    val referencingPairs = findSoPPairsReferencingPhraseInFactors(phrase,sopPairs.asScala.toList)
    referencingPairs.map(p => p.getConditionName).mkString(Properties.lineSeparator)
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

  private def findSoPPairsReferencingPhraseInFactors(phrase: String, sopPairs: List[SoPPair]): List[SoPPair] = {

    def definedTermsContainsPhrase(conditionName: String,  factor: Factor) = {
            val definitions = factor.getDefinedTerms.asScala
            definitions.exists(d => d.getDefinition.contains(phrase))
    }

    def findFactorsContainingConditionName(conditionName: String, SoPPair: SoPPair): List[Factor] = {
      val allFactors: List[Factor] = (SoPPair.getRhSop.getOnsetFactors.asScala
        ++ SoPPair.getRhSop.getAggravationFactors.asScala
        ++ SoPPair.getBopSop.getOnsetFactors.asScala
        ++ SoPPair.getBopSop.getAggravationFactors.asScala) toList

      allFactors.filter(f => f.getText.contains(conditionName) || definedTermsContainsPhrase(conditionName, f))
    }

    sopPairs.filter(SoPPair => findFactorsContainingConditionName(phrase, SoPPair).nonEmpty)
  }

  private def createEdgesForSoPPair(sopPair : SoPPair, otherSoPPairs : List[SoPPair]): Seq[LDiEdge[SoPPair]] = {
    val sources = findSoPPairsReferencingPhraseInFactors(sopPair.getConditionName,otherSoPPairs)
    val target = sopPair
    sources map (s => LDiEdge(s,target)(""))
  }

  private def liftSubGraphForCondition(graph : Graph[SoPPair, LDiEdge], condition : SoPPair): Graph[SoPPair,LDiEdge] = {
    val root = graph get condition
    val subGraph = root.innerNodeTraverser.toGraph
    subGraph
  }

  // need function to link a factor to a condition, including in definitions
  def getConditionDependenciesOfFactor(sourceSop: SoP, sourceFactor: Factor, conditionNames : Set[String]) : Set[FactorRef]  = {
    // links can be in factor text or defined terms
    // link can require onset by a certain date
    ???
  }

}

