package au.gov.dva.sopapi.sopref.dependencies

import java.time.{Duration, LocalDate, OffsetDateTime, Period}
import java.time.format.DateTimeFormatter

import au.gov.dva.sopapi.dtos.StandardOfProof
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

abstract class InstantCondition(soPPair: SoPPair, onsetDate: LocalDate){
  def getSoPair = soPPair
  def getOnsetDate = onsetDate
}
case class AcceptedCondition(soPPair: SoPPair, acceptedStandardOfProof : StandardOfProof, acceptedFactor : Factor, onsetDate: LocalDate) extends InstantCondition(soPPair,onsetDate)
case class DiagnosedCondition(soPPair: SoPPair, applicableStandardOfProof: StandardOfProof, isOnset: Boolean,  date : LocalDate) extends InstantCondition(soPPair,date)

case class SopNode(soPPair: SoPPair, instantCondition: Option[InstantCondition])

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

  def buildGraphP(sopPairs : ImmutableSet[SoPPair]) = {
    val g = buildGraph(sopPairs.asScala.toList)
    g
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
    val subGraph = root.innerNodeTraverser.withDirection(Predecessors).withMaxDepth(2).toGraph
    subGraph
  }

  def getShortestPathFromAcceptedToDiagnosed(diagnosed: SoPPair, accepted: SoPPair, graph:  Graph[SoPPair,LDiEdge]) = {
    val startNode = graph get diagnosed
    val targetNode = graph get accepted
    startNode shortestPathTo targetNode
  }

  // take diagnosed conditions and date
  // return paths with hop of one step

  // build graph containing only diagnosed and accepted conditions
  // topo sort to find order

  def topoSort(graph: Graph[SoPPair,LDiEdge]) = {
    graph.topologicalSort
  }

  def getInstantGraph(acceptedConditions: List[AcceptedCondition], diagnosedConditions: List[DiagnosedCondition]) = {


    def parsePeriodFromFactor(factorText: String, sourceConditionName: String) : Option[Period]  = {

      def toInt(s: String): Option[Int] = {
        try {
          Some(s.toInt)
        } catch {
          case e: Exception => None
        }
      }

      def tryParseNumber(numberString : String) = {
        numberString match {
          case "one" => Some(1)
          case "two" => Some(2)
          case "three" => Some(3)
          case "four" => Some(4)
          case "five" => Some(5)
          case "six" => Some(6)
          case "seven" => Some(7)
          case "eight" => Some(8)
          case "nine" => Some(9)
          case _ => toInt(numberString)
        }
      }

      val regex = s"""within the ([a-z0-9]+) (days|months|years) before the clinical onset of(\\sthe\\s)?$sourceConditionName""".r
      regex.findFirstMatchIn(factorText) match {
        case Some(v) => {
          tryParseNumber(v.group(1)) match {
            case Some(n) => {
              v.group(2) match {
                case "days" => Some(Period.of(0, 0, n))
                case "months" => Some(Period.of(0, n, 0))
                case "years" => Some(Period.of(n, 0, 0))
                case _ => throw new IllegalArgumentException
              }}
             case _ => None
            }
        }
        case _ => None

    }
    }


    def canTraverse(edgeLabel : FactorRefForSoPPair): Boolean = {
      // target must be accepted
      // if edge has condition variant, accepted factor must be for that variant
      // source must be diagnosed or accepted
      // accepted condition must be before diagnosed condition
      // if there is variant, check the factor of the diagnosed condition

      val sopToCondition = (acceptedConditions ++ diagnosedConditions).map(ic => (ic.getSoPair -> ic)).toMap

      def isOnsetFactor(factorPara: String, sop: SoP) = sop.getOnsetFactors.asScala.map(f => f.getParagraph).contains(factorPara)


      val sourceInstantCondition = sopToCondition(edgeLabel.dependentSoPPair)
      def findLinkingFactor(source: DiagnosedCondition) = {
        val diagnosed = source
        val referringStandardOfProof = diagnosed.applicableStandardOfProof
        val isOnset = diagnosed.isOnset
        val linkingFactor = referringStandardOfProof match {
          case StandardOfProof.ReasonableHypothesis => isOnset match {
            case true => edgeLabel.rhRefs.find(fr => isOnsetFactor(fr.linkingFactor.getParagraph,edgeLabel.dependentSoPPair.getRhSop))
            case false => edgeLabel.rhRefs.find(fr => !isOnsetFactor(fr.linkingFactor.getParagraph,edgeLabel.dependentSoPPair.getRhSop))
          }
          case StandardOfProof.BalanceOfProbabilities => isOnset match {
            case true => edgeLabel.rhRefs.find(fr => isOnsetFactor(fr.linkingFactor.getParagraph,edgeLabel.dependentSoPPair.getBopSop))
            case false => edgeLabel.rhRefs.find(fr => !isOnsetFactor(fr.linkingFactor.getParagraph,edgeLabel.dependentSoPPair.getBopSop))
          }
        }
        linkingFactor
      }


      val targetInstantCondition: InstantCondition = sopToCondition(edgeLabel.targetSoPPair)
      val targetIsAccepted = targetInstantCondition.isInstanceOf[AcceptedCondition]
      val targetOccuredBeforeSource = targetInstantCondition.getOnsetDate.isBefore(sourceInstantCondition.getOnsetDate)


      def targetConditionWithinPeriodFromSource(sourceDiagnosed : DiagnosedCondition, target: AcceptedCondition, period: Period) = {
        val diagnosedMinusPeriod: LocalDate = sourceDiagnosed.getOnsetDate.minus(period)
        !target.onsetDate.isBefore(diagnosedMinusPeriod)
      }
      // if source is diagnosed, check time limit

      def checkTimeLimitOnDiagnosed(sourceDiagnosed : DiagnosedCondition) : Boolean = {
        val linkingFactor = findLinkingFactor(sourceDiagnosed).get
        val timeLimitRequirement = parsePeriodFromFactor(linkingFactor.linkingFactor.getText,edgeLabel.dependentSoPPair.getConditionName)
        timeLimitRequirement match
        {
          case Some(v) => targetConditionWithinPeriodFromSource(sourceInstantCondition.asInstanceOf[DiagnosedCondition],targetInstantCondition.asInstanceOf[AcceptedCondition],v)
          case None => true
        }
      }

      sourceInstantCondition.isInstanceOf[DiagnosedCondition] match {
        case true => targetIsAccepted && targetOccuredBeforeSource && checkTimeLimitOnDiagnosed(sourceInstantCondition.asInstanceOf[DiagnosedCondition])
        case false => targetIsAccepted && targetOccuredBeforeSource
      }

    }
    // check if edge can be traversed
    val sopPairs = acceptedConditions.map(ac => ac.soPPair) ++ diagnosedConditions.map(dc => dc.soPPair)
    val graph: Graph[SoPPair, LDiEdge] = buildGraph(sopPairs)
    // find edges that cannot be traversed
    // create subgraph without those edges

    val edges = graph.edges.map(_.toOuter).filter(e => {
      val label = e.label.asInstanceOf[FactorRefForSoPPair]
      // todo: filter out nodes that can't be traversed because of onset dates or accepted factors
      canTraverse(label)
    })
    val nodesInEdges = edges.flatMap(e => e.sources ++ e.targets)

    Graph.from(nodesInEdges,edges)
  }

}

