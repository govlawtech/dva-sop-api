package au.gov.dva.sopapi.sopref.dependencies

import java.io.{ByteArrayOutputStream, OutputStream}
import java.time.{Duration, LocalDate, OffsetDateTime, Period}
import java.time.format.DateTimeFormatter

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.dtos.sopref.{FactorDto, ICDCodeDto}
import au.gov.dva.sopapi.dtos.sopsupport.components.OnsetDateRangeDto
import au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance._
import au.gov.dva.sopapi.interfaces.model.{Factor, ICDCode, SoP, SoPPair}
import au.gov.dva.sopapi.sopref.DtoTransformations
import au.gov.dva.sopapi.sopref.dependencies.Dependencies.liftSubGraphForCondition
import au.gov.dva.sopapi.sopref.parsing.traits.MiscRegexes
import com.google.common.collect.{ImmutableList, ImmutableSet, Sets}
import org.apache.commons.lang.WordUtils
import org.joda.time.format.{PeriodFormat, PeriodFormatterBuilder}
import org.w3c.dom.traversal.NodeFilter
import scalax.collection.GraphEdge.DiEdge

import collection.JavaConverters._
import scala.util.Properties
import scalax.collection.io.dot._
import scalax.collection.{Graph, GraphEdge}
import scalax.collection.GraphTraversal.{Predecessors, Successors}
import scalax.collection.edge.LDiEdge
import scalax.collection.io.dot
import scalax.collection.io.dot.Record

import scala.collection.immutable


case class FactorRef(linkingFactor: Factor, period: Option[Period]) {



  override def toString: String = {
    def prettyPrintPeriod(p: Period) = {
      if (p.getYears > 0) s"${p.getYears} years"
      else if (p.getMonths > 0) s"${p.getMonths} months"
      else if (p.getDays > 0) s"${p.getDays} days"
      else p.toString
    }

    val periodOrEmpty = if (period.isDefined) s" (${prettyPrintPeriod(period.get)})" else ""
    if (linkingFactor.getConditionVariant.isPresent)
      s"${linkingFactor.getParagraph} (${linkingFactor.getConditionVariant.get().getName})$periodOrEmpty"
    else s"${linkingFactor.getParagraph}$periodOrEmpty"
  }


}

case class FactorRefForSoPPair(dependentSoPPair: SoPPair, targetSoPPair: SoPPair, rhRefs: List[FactorRef], bopRefs: List[FactorRef]) {
  override def toString =
    {

      def refsToString(rs : List[FactorRef], standardOfProof: StandardOfProof) = {
        rs match  {
          case List() => None
          case _ => Some(s"${standardOfProof.toAbbreviatedString}: ${rs.mkString(", ")}")
        }
      }

    List(refsToString(rhRefs,StandardOfProof.ReasonableHypothesis), refsToString(bopRefs, StandardOfProof.BalanceOfProbabilities)).filter(_.isDefined).map(_.get).mkString("; ")
      }
}

abstract class InstantCondition(soPPair: SoPPair, onsetDate: LocalDate, iCDCodeOpt: Option[ICDCodeDto], SideOpt: Option[Side]) {
  def getSoPair = soPPair

  def getOnsetDate = onsetDate

  def getIcdCode = iCDCodeOpt

  def getSide = SideOpt
}

case class AcceptedCondition(soPPair: SoPPair, acceptedStandardOfProof: StandardOfProof, onsetDate: LocalDate, iCDCodeOpt: Option[ICDCodeDto] = None, SideOpt: Option[Side] = None) extends InstantCondition(soPPair, onsetDate, iCDCodeOpt, SideOpt)

case class DiagnosedCondition(soPPair: SoPPair, isOnset: Boolean, date: LocalDate, iCDCodeOpt: Option[ICDCodeDto] = None, SideOpt: Option[Side] = None)


  extends InstantCondition(soPPair, date, iCDCodeOpt, SideOpt)

object InstantConditions {

  def decomposeRequestDto(request: SequelaeRequestDto, sopPairs: ImmutableSet[SoPPair]) = {

    val scalaSoPSet = sopPairs.asScala.map(s => s.getConditionName -> s).toMap
    val accepted = request.get_acceptedConditions().asScala.map(ac => acceptedConditionFromDto(ac, scalaSoPSet(ac.get_name())))
    val diagnosed = request.get_diagnosedConditions().asScala.map(ac => diagnosedConditionFromDto(ac, scalaSoPSet(ac.get_name())))
    (accepted, diagnosed)
  }

  def acceptedConditionFromDto(dto: AcceptedConditionDto, sp: SoPPair): AcceptedCondition = {
    AcceptedCondition(sp, dto.get_standardOfProof(), dto.get_date(), Option(dto.get_icdCode()), Option(dto.get_side()))
  }

  def diagnosedConditionFromDto(dto: DiagnosedConditionDto, sp: SoPPair): DiagnosedCondition = {
    DiagnosedCondition(sp, dto.get_isOnset(), dto.get_date(), Option(dto.get_icdCode()), Option(dto.get_side()))
  }
}


case class SopNode(soPPair: SoPPair, instantCondition: Option[InstantCondition])

class Dependencies(val  sopPairs : ImmutableSet[SoPPair])
{
  val cachedGraph = Dependencies.buildWholeGraph(sopPairs)

  def getDotSubgraph(rootConditionName: String, steps: Int = 3): String = {
    val testCondition = sopPairs.asScala.find(sp => sp.getConditionName == rootConditionName).head
    val subGraph = liftSubGraphForCondition(cachedGraph, testCondition, steps)
    Dependencies.toDotString(subGraph)
  }

}


object Dependencies extends MiscRegexes {


  def buildWholeGraph(sopPairs: ImmutableSet[SoPPair]): Graph[SoPPair,LDiEdge] = {

    val g = buildGraph(sopPairs.asScala.toList)
    g
  }

  def toDotString(graph: Graph[SoPPair, LDiEdge]) = {
    val dotRoot = DotRootGraph(
      directed = true,
      id = Some(Id(s"SoP Dependencies Graph Generated ${DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())}")),
      attrStmts = List(
        DotAttrStmt(Elem.node, List(DotAttr(Id("shape"), Id("record")))),
        DotAttrStmt(Elem.node, List(DotAttr(Id("shape"), Id("record")))),
        DotAttrStmt(Elem.graph, List(DotAttr(Id("rankdir"), Id("LR"))))
      )
    )

    def nodeTransformer(innerNode: Graph[SoPPair,LDiEdge]#NodeT): Option[(DotGraph,DotNodeStmt)] = {
      val sp = innerNode.toOuter
      val labelString = s"${sp.getConditionName}|RH: ${sp.getRhSop.getRegisterId}|BoP: ${sp.getBopSop.getRegisterId}"
      Some(dotRoot, DotNodeStmt(NodeId(innerNode.toOuter.getConditionName),
        Seq(DotAttr(Id("label"),Id(labelString)))
      ))
    }
    def edgeTransformer(innerEdge: Graph[SoPPair, LDiEdge]#EdgeT): Option[(DotGraph, DotEdgeStmt)] = {
      innerEdge.edge match {
        case LDiEdge(source, target, label) => Some(
          (dotRoot, DotEdgeStmt(
            NodeId(source.toString()),
            NodeId(target.toString()),
            Seq(
              DotAttr(Id("label"), Id(WordUtils.wrap(label.toString, 100)))
            )
          )))
      }
    }

    graph.toDot(dotRoot, edgeTransformer,None,Some(nodeTransformer),Some(nodeTransformer))
  }

  private def buildGraph(SoPPairs: List[SoPPair]): Graph[SoPPair, LDiEdge] = {
    val edges = buildEdges(SoPPairs)
    val g: Graph[SoPPair, LDiEdge] = Graph.from(SoPPairs, edges)
    g
  }

  private def buildEdges(SoPPairs: List[SoPPair]): Seq[LDiEdge[SoPPair]] = {
    def createEdgesForSoPPairRecursive(remainingSoPPairs: List[SoPPair]): Seq[LDiEdge[SoPPair]] = {
      if (remainingSoPPairs.isEmpty) List()
      else createEdgesForSoPPair(remainingSoPPairs.head, SoPPairs.filter(s => s.getConditionName != remainingSoPPairs.head.getConditionName)) ++ createEdgesForSoPPairRecursive(remainingSoPPairs.tail)
    }

    createEdgesForSoPPairRecursive(SoPPairs)
  }

  private def findFactorReferences(dependentSoPPair: SoPPair, otherSoPPairs: List[SoPPair]): List[FactorRefForSoPPair] = {
    def definedTermsContainsPhrase(conditionName: String, factor: Factor) = {
      val definitions = factor.getDefinedTerms.asScala
      definitions.exists(d => d.getDefinition.contains(conditionName))
    }


    def textContainsPhraseWithoutNegation(phrase: String, text: String): Boolean = {


      val textWithLineBreaksReplaced = text.replaceAll(platformNeutralLineEndingRegex.regex, " ")

      def testPhrase(phrase: String): Boolean = {
        val phraseMatches = textWithLineBreaksReplaced.contains(phrase)
        if (!phraseMatches)
          return false
        val phrasePreceededByNegation = s"""(other than|excepting|excluding|except for|does not involve( a )?)$phrase""".r
        if (phrasePreceededByNegation.findFirstMatchIn(text).isDefined)
          return false
        else return true
      }

      testPhrase(phrase)


    }

    def gatherFactorRefs(dependentSop: SoP, targetSopPair: SoPPair): List[FactorRef] = {
      def getFactorsReferencesingCondition(factors: List[Factor]) = factors.filter(f => textContainsPhraseWithoutNegation(targetSopPair.getConditionName, f.getText) || definedTermsContainsPhrase(targetSopPair.getConditionName, f))

      def toFactorRef(f: Factor) = FactorRef(f, parsePeriodFromFactor(f.getText, dependentSop.getConditionName))

      val onsetFactorsReferencingCondition = getFactorsReferencesingCondition(dependentSop.getOnsetFactors.asScala.toList)
      val aggFactorsReferencingCondition = getFactorsReferencesingCondition(dependentSop.getAggravationFactors.asScala.toList)
      (onsetFactorsReferencingCondition ++ aggFactorsReferencingCondition).map(toFactorRef(_))
    }

    def getFactorRefForSopPair(dependentSoPPair: SoPPair, targetSoPPair: SoPPair): FactorRefForSoPPair = {
      val rhRefs = gatherFactorRefs(dependentSoPPair.getRhSop, targetSoPPair)
      val bopRefs = gatherFactorRefs(dependentSoPPair.getBopSop, targetSoPPair)
      FactorRefForSoPPair(dependentSoPPair, targetSoPPair, rhRefs, bopRefs)
    }

    otherSoPPairs.map(ts => getFactorRefForSopPair(dependentSoPPair, ts))
  }

  private def createEdgesForSoPPair(sopPair: SoPPair, otherSoPPairs: List[SoPPair]): immutable.Seq[LDiEdge[SoPPair] with GraphEdge.EdgeCopy[LDiEdge] {
    type L1 = FactorRefForSoPPair
  }] = {
    val factorReferencesFromDependentSop = findFactorReferences(sopPair, otherSoPPairs).filter(fr => !fr.bopRefs.isEmpty || !fr.rhRefs.isEmpty)
    factorReferencesFromDependentSop.map(fr => LDiEdge(fr.dependentSoPPair, fr.targetSoPPair)(fr))
  }

  private def liftSubGraphForCondition(graph: Graph[SoPPair, LDiEdge], condition: SoPPair, steps: Int): Graph[SoPPair, LDiEdge] = {
    val root = graph get condition
    val subGraph1 = root.innerNodeTraverser.withDirection(Predecessors).withMaxDepth(steps).toGraph
    val subGraph2 = root.innerNodeTraverser.withDirection(Successors).withMaxDepth(steps).toGraph
    val subGraph = subGraph1 union subGraph2
    subGraph
  }

  def getShortestPathFromAcceptedToDiagnosed(diagnosed: SoPPair, accepted: SoPPair, graph: Graph[SoPPair, LDiEdge]) = {
    val startNode = graph get diagnosed
    val targetNode = graph get accepted
    startNode shortestPathTo targetNode
  }

  // take diagnosed conditions and date
  // return paths with hop of one step

  // build graph containing only diagnosed and accepted conditions
  // topo sort to find order


  def parsePeriodFromFactor(factorText: String, sourceConditionName: String): Option[Period] = {

    def toInt(s: String): Option[Int] = {
      try {
        Some(s.toInt)
      } catch {
        case e: Exception => None
      }
    }

    def tryParseNumber(numberString: String) = {
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

    val regex = s"""within the ([a-z0-9]+) (days|months|years) before the clinical onset of (the )?$sourceConditionName""".r
    regex.findFirstMatchIn(factorText) match {
      case Some(v) => {
        tryParseNumber(v.group(1)) match {
          case Some(n) => {
            v.group(2) match {
              case "days" => Some(Period.of(0, 0, n))
              case "months" => Some(Period.of(0, n, 0))
              case "years" => Some(Period.of(n, 0, 0))
              case _ => throw new IllegalArgumentException
            }
          }
          case _ => None
        }
      }
      case _ => None

    }
  }


  def getPathForStandardOfProof(graph: Graph[SoPPair, LDiEdge], targetSoPPair: SoPPair, sourceSoPPair: SoPPair, standardOfProof: StandardOfProof, canTraverse: (FactorRefForSoPPair, StandardOfProof) => Boolean) = {
    // return path
    val sourceNode = graph get sourceSoPPair
    val targetNode = graph get targetSoPPair

    def edgeFilter(e: graph.EdgeT): Boolean = {
      val label = e.label.asInstanceOf[FactorRefForSoPPair]
      canTraverse(label,standardOfProof) match {
        case true => {
          standardOfProof match {
            case StandardOfProof.ReasonableHypothesis => label.rhRefs.nonEmpty
            case StandardOfProof.BalanceOfProbabilities => label.bopRefs.nonEmpty
          }
        }
        case false => false
      }
    }

    sourceNode.withSubgraph(edges = edgeFilter).pathTo(targetNode)
  }

  def getPaths(graph: Graph[SoPPair, LDiEdge], acceptedConditions: Set[AcceptedCondition], diagnosedConditions: Set[DiagnosedCondition]) = {
    // for each diagnosed condition, check paths to accepted conditions using the accepted condition's standard of proof
    def canTraverse(edgeLabel: FactorRefForSoPPair, acceptedConditions: List[AcceptedCondition], diagnosedConditions: List[DiagnosedCondition], standardOfProof: StandardOfProof): Boolean = {
      val sopToCondition = (acceptedConditions ++ diagnosedConditions).map(ic => (ic.getSoPair -> ic)).toMap
      val sourceInstantCondition = sopToCondition(edgeLabel.dependentSoPPair)

      def isOnsetFactor(factorPara: String, sop: SoP) = sop.getOnsetFactors.asScala.map(f => f.getParagraph).contains(factorPara)


      def findLinkingFactor(source: DiagnosedCondition, standardOfProof: StandardOfProof) = {
        val diagnosed = source

        val referringStandardOfProof = standardOfProof
        val isOnset = diagnosed.isOnset
        val linkingFactor = referringStandardOfProof match {
          case StandardOfProof.ReasonableHypothesis => isOnset match {
            case true => edgeLabel.rhRefs.find(fr => isOnsetFactor(fr.linkingFactor.getParagraph, edgeLabel.dependentSoPPair.getRhSop))
            case false => edgeLabel.rhRefs.find(fr => !isOnsetFactor(fr.linkingFactor.getParagraph, edgeLabel.dependentSoPPair.getRhSop))
          }
          case StandardOfProof.BalanceOfProbabilities => isOnset match {
            case true => edgeLabel.rhRefs.find(fr => isOnsetFactor(fr.linkingFactor.getParagraph, edgeLabel.dependentSoPPair.getBopSop))
            case false => edgeLabel.rhRefs.find(fr => !isOnsetFactor(fr.linkingFactor.getParagraph, edgeLabel.dependentSoPPair.getBopSop))
          }
        }
        linkingFactor
      }

      val targetInstantCondition: InstantCondition = sopToCondition(edgeLabel.targetSoPPair)

      def targetConditionWithinPeriodFromSource(sourceDiagnosed: DiagnosedCondition, target: AcceptedCondition, period: Period) = {
        val diagnosedMinusPeriod: LocalDate = sourceDiagnosed.getOnsetDate.minus(period)
        !target.onsetDate.isBefore(diagnosedMinusPeriod)
      }

      def checkTimeLimitOnDiagnosed(sourceDiagnosed: DiagnosedCondition, standardOfProof: StandardOfProof): Boolean = {

        val linkingFactor = findLinkingFactor(sourceDiagnosed, standardOfProof).get
        val timeLimitRequirement = parsePeriodFromFactor(linkingFactor.linkingFactor.getText, edgeLabel.dependentSoPPair.getConditionName)
        timeLimitRequirement match {
          case Some(v) => targetConditionWithinPeriodFromSource(sourceInstantCondition.asInstanceOf[DiagnosedCondition], targetInstantCondition.asInstanceOf[AcceptedCondition], v)
          case None => true
        }
      }

      val dependentIsOnOrAfterTarget = !sourceInstantCondition.getOnsetDate.isBefore(targetInstantCondition.getOnsetDate)

      val anyTimeLimitMet = checkTimeLimitOnDiagnosed(sourceInstantCondition.asInstanceOf[DiagnosedCondition], standardOfProof)

      dependentIsOnOrAfterTarget && anyTimeLimitMet

    }

    def canTraversePartial(edgeLabel: FactorRefForSoPPair, standardOfProof: StandardOfProof) = {
      canTraverse(edgeLabel, acceptedConditions.toList, diagnosedConditions.toList, standardOfProof)
    }

    val paths = for (d <- diagnosedConditions;
         a <- acceptedConditions)
      yield getPathForStandardOfProof(graph, a.soPPair, d.soPPair, a.acceptedStandardOfProof, canTraversePartial)

    paths.filter(_.isDefined).map(_.get)
  }


  def getInstantGraph(acceptedConditions: List[AcceptedCondition], diagnosedConditions: List[DiagnosedCondition], testEdgeTraverse: Boolean): Graph[SoPPair, LDiEdge] = {
    val sopPairs = acceptedConditions.map(ac => ac.soPPair) ++ diagnosedConditions.map(dc => dc.soPPair)
    val graph: Graph[SoPPair, LDiEdge] = buildGraph(sopPairs)
    graph
  }


  def findNodesInCycles(graph: Graph[SoPPair, LDiEdge]) = {
    graph.nodes
      .filter(n => graph.findCycleContaining(n).isDefined)
      .map(n => n.toOuter)
  }

  def getPrettyPrintedReasonsForSequela(path: Graph[SoPPair,LDiEdge]#Path, accepted: Set[AcceptedCondition], diagnosed: Set[DiagnosedCondition], shouldAutoAccept : ((String,String) => Boolean)) = {

    val icsMap = (accepted ++ diagnosed).map(i => i.getSoPair.getConditionName -> i).toMap

    def prettyPrintConditionInstance(ic : InstantCondition) = {
      val date = DateTimeFormatter.ISO_LOCAL_DATE.format(ic.getOnsetDate)
      s"the ${ic.getSoPair.getConditionName} with date of $date"
    }

    val diagnosedCondition = prettyPrintConditionInstance(icsMap(path.startNode.toOuter.getConditionName))
    val acceptedCondition = prettyPrintConditionInstance(icsMap(path.endNode.toOuter.getConditionName))
    val standardOfProof = icsMap(path.endNode.toOuter.getConditionName).asInstanceOf[AcceptedCondition].acceptedStandardOfProof

    def prettyPrintSoPFactors(edgeLabel: FactorRefForSoPPair, standardOfProof: StandardOfProof) = {
      val registerId = standardOfProof match {
        case StandardOfProof.ReasonableHypothesis => edgeLabel.dependentSoPPair.getRhSop.getRegisterId
        case StandardOfProof.BalanceOfProbabilities => edgeLabel.dependentSoPPair.getBopSop.getRegisterId
      }
      val factors = standardOfProof match {
        case StandardOfProof.ReasonableHypothesis => edgeLabel.rhRefs.map(f => f.linkingFactor.getParagraph).mkString(", ")
        case StandardOfProof.BalanceOfProbabilities => edgeLabel.bopRefs.map(f => f.linkingFactor.getParagraph).mkString(", ")
      }
      s"${factors} in instrument ${registerId}"
    }

    def prettyPrintAllRelevantSoPFactors() = {
      path.edges.map(e => prettyPrintSoPFactors(e.label.asInstanceOf[FactorRefForSoPPair],standardOfProof)).mkString("; ")
    }

    val mainRecommendationSentenceForAccept = s"Accept initial liability for $diagnosedCondition.  It is direct or indirect sequela of an accepted condition: $acceptedCondition.  The standard of proof is the same as the accepted condition: $standardOfProof.  The relevant SoP factors are: $prettyPrintAllRelevantSoPFactors."

    val mainRecommendationForReview = s"Review whether to accept initial liability for $diagnosedCondition on the grounds that it is a direct or indirect sequela of an accepted condition: $acceptedCondition.  This depends on whether qualifications in the relevant SoP factors are met.  The standard of proof is the same as the accepted condition: $standardOfProof.  The relevant SoP factors are: $prettyPrintAllRelevantSoPFactors."

    shouldAutoAccept(path.startNode.toOuter.getConditionName, path.endNode.toOuter.getConditionName) match {
      case true => mainRecommendationSentenceForAccept
      case false => mainRecommendationForReview
    }

  }

  def getSvg(dto: SequelaeDiagramRequestDto, allSoPPairs: ImmutableSet[SoPPair]) = {
    val (accepted, diagnosed) = (dto.get_acceptedConditions(),dto.get_diagnosedConditions())
    val allConditionNames = accepted.asScala ++ diagnosed.asScala
    val nameToSoPPairs = allSoPPairs.asScala.map(sp => sp.getConditionName -> sp).toMap
    val sopsToGraph = allConditionNames.map(c => nameToSoPPairs(c))
    val graph: Graph[SoPPair, LDiEdge] = buildGraph(sopsToGraph.toList)
    val dotString = Dependencies.toDotString(graph)
    val os = new ByteArrayOutputStream()
    DotToImage.render(dotString, os)
    os.close()
    os.toByteArray
  }


  def getInferredSequelae(dto: SequelaeRequestDto, soPPairs: ImmutableSet[SoPPair]): AcceptedSequalaeResponse = {
    val (accepted, diagnosed) = InstantConditions.decomposeRequestDto(dto, soPPairs)
    val graph: Graph[SoPPair, LDiEdge] = getInstantGraph(accepted.toList, diagnosed.toList, true)
    val paths = getPaths(graph, accepted.toSet, diagnosed.toSet)

    val recommendations = paths.map(getPrettyPrintedReasonsForSequela(_,accepted.toSet,diagnosed.toSet,Configuration.shouldAccept))
    val diagnosedConditionNames = diagnosed.map(c => c.soPPair.getConditionName).toSet

    val edgesFromSequelae: Set[Graph[SoPPair, LDiEdge]#EdgeT] = paths.map(p => p.edges.head)

    def factorRefToDto(ref: FactorRefForSoPPair): AcceptedSequalaeResponseConditionDto = {
      new AcceptedSequalaeResponseConditionDto(
        ref.dependentSoPPair.getConditionName,
        new FactorListDto(ref.dependentSoPPair.getRhSop.getRegisterId, ref.rhRefs.map(r => DtoTransformations.fromFactorToLink(r.linkingFactor)).asJava),
        new FactorListDto(ref.dependentSoPPair.getBopSop.getRegisterId, ref.bopRefs.map(r => DtoTransformations.fromFactorToLink(r.linkingFactor)).asJava)
      )
    }

    val inferredSequelaeDtos = edgesFromSequelae.map(e => {
      val label = e.label.asInstanceOf[FactorRefForSoPPair]
      val dto = factorRefToDto(label)
      dto
    })

    // remove nodes in cycles for the purpose of topo sorting
    val nodesInCycles = findNodesInCycles(graph)
    val graphWithCycleNodesRemoved = nodesInCycles.foldLeft(graph)((acc, n) => acc - n)

    val topoSortedConditions = graphWithCycleNodesRemoved.topologicalSort.fold(cycleNode => None, order => Some(order))
    val orderOfApplication = topoSortedConditions match {
      case None => List()
      case Some(o) => o.toList.map(n => n.toOuter.asInstanceOf[SoPPair].getConditionName).filter(n => diagnosedConditionNames.contains(n)).reverse
    }

    new AcceptedSequalaeResponse(recommendations.toList.asJava, inferredSequelaeDtos.toList.asJava, orderOfApplication.asJava)

  }

}

