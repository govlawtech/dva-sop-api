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


    override def toString: String =  {
      def prettyPrintPeriod(p : Period) = {
        if (p.getYears > 0) s"${p.getYears} years"
        else if (p.getMonths > 0)  s"${p.getMonths} months"
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
  override def toString = if (rhRefs != bopRefs) s"RH: ${rhRefs.mkString(", ")}; BoP: ${bopRefs.mkString(", ")}" else s"${rhRefs.mkString(", ")}"
}

abstract class InstantCondition(soPPair: SoPPair, onsetDate: LocalDate, iCDCodeOpt: Option[ICDCodeDto], SideOpt: Option[Side]){
  def getSoPair = soPPair
  def getOnsetDate = onsetDate
  def getIcdCode  = iCDCodeOpt
  def getSide = SideOpt
}
case class AcceptedCondition(soPPair: SoPPair, acceptedStandardOfProof: StandardOfProof,  onsetDate: LocalDate, iCDCodeOpt: Option[ICDCodeDto] = None, SideOpt: Option[Side] = None) extends InstantCondition(soPPair,onsetDate,iCDCodeOpt,SideOpt)

case class DiagnosedCondition(soPPair: SoPPair, isOnset: Boolean,  date : LocalDate, iCDCodeOpt: Option[ICDCodeDto] = None, SideOpt: Option[Side] = None)


extends InstantCondition(soPPair,date, iCDCodeOpt, SideOpt)

object InstantConditions {

  def decomposeRequestDto(request : SequelaeRequestDto, sopPairs : ImmutableSet[SoPPair]) = {

    val scalaSoPSet = sopPairs.asScala.map(s => s.getConditionName -> s).toMap
    val accepted = request.get_acceptedConditions().asScala.map(ac => acceptedConditionFromDto(ac,scalaSoPSet(ac.get_name())))
    val diagnosed = request.get_diagnosedConditions().asScala.map(ac => diagnosedConditionFromDto(ac,scalaSoPSet(ac.get_name())))
    (accepted,diagnosed)
  }

  def acceptedConditionFromDto(dto : AcceptedConditionDto, sp : SoPPair): AcceptedCondition = {
    AcceptedCondition(sp, dto.get_standardOfProof(),  dto.get_date(),Option(dto.get_icdCode()),Option(dto.get_side()))
  }
  def diagnosedConditionFromDto(dto : DiagnosedConditionDto, sp : SoPPair) : DiagnosedCondition = {
    DiagnosedCondition(sp,dto.get_isOnset(),dto.get_date(),Option(dto.get_icdCode()),Option(dto.get_side()))
  }
}


case class SopNode(soPPair: SoPPair, instantCondition: Option[InstantCondition])

object Dependencies extends MiscRegexes {

  def toDotString(graph: Graph[SoPPair,LDiEdge]) = {
    val dotRoot = DotRootGraph(
      directed = true,
      id = Some(Id(s"SoP Dependencies Graph Generated ${DateTimeFormatter.ISO_DATE_TIME.format(OffsetDateTime.now())}")),
      attrStmts = List(
        DotAttrStmt(Elem.node, List(DotAttr(Id("shape"), Id("record")))),
        DotAttrStmt(Elem.node, List(DotAttr(Id("rankdir"), Id("LR"))))

      )
    )



    def edgeTransformer(innerEdge: Graph[SoPPair,LDiEdge]#EdgeT): Option[(DotGraph,DotEdgeStmt)] = {
      innerEdge.edge match {
        case LDiEdge(source,target,label) => Some(
          (dotRoot,DotEdgeStmt(
            NodeId(source.toString()),
            NodeId(target.toString()),
            Seq(
              DotAttr(Id("label"),Id(WordUtils.wrap(label.toString,100)))
            )
          )))
      }
    }
    graph.toDot(dotRoot, edgeTransformer)

  }

  def buildDotStringForAll(sopPairs : ImmutableSet[SoPPair]) : String = {
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
    g.toDot(dotRoot, edgeTransformer)
  }

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



    def textContainsPhraseWithoutNegation(phrase: String, text: String): Boolean = {


      val textWithLineBreaksReplaced = text.replaceAll(platformNeutralLineEndingRegex.regex, " ")

      def testPhrase(phrase : String) : Boolean = {
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

    def gatherFactorRefs(dependentSop: SoP, targetSopPair: SoPPair) : List[FactorRef] = {
      def getFactorsReferencesingCondition(factors: List[Factor] ) = factors.filter(f => textContainsPhraseWithoutNegation(targetSopPair.getConditionName,f.getText) || definedTermsContainsPhrase(targetSopPair.getConditionName,f))
      def toFactorRef(f : Factor) = FactorRef(f,parsePeriodFromFactor(f.getText,dependentSop.getConditionName))
      val onsetFactorsReferencingCondition = getFactorsReferencesingCondition(dependentSop.getOnsetFactors.asScala.toList)
      val aggFactorsReferencingCondition = getFactorsReferencesingCondition(dependentSop.getAggravationFactors.asScala.toList)
      (onsetFactorsReferencingCondition ++ aggFactorsReferencingCondition).map(toFactorRef(_))
    }

    def getFactorRefForSopPair(dependentSoPPair : SoPPair, targetSoPPair : SoPPair) : FactorRefForSoPPair = {
      val rhRefs = gatherFactorRefs(dependentSoPPair.getRhSop,targetSoPPair)
      val bopRefs = gatherFactorRefs(dependentSoPPair.getBopSop,targetSoPPair)
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
    val subGraph = root.innerNodeTraverser.withDirection(Predecessors).withMaxDepth(3).toGraph
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
            }}
          case _ => None
        }
      }
      case _ => None

    }
  }



  def canTraverse(edgeLabel: FactorRefForSoPPair, acceptedConditions : List[AcceptedCondition], diagnosedConditions: List[DiagnosedCondition]): Boolean = {
    val sopToCondition = (acceptedConditions ++ diagnosedConditions).map(ic => (ic.getSoPair -> ic)).toMap
    def isOnsetFactor(factorPara: String, sop: SoP) = sop.getOnsetFactors.asScala.map(f => f.getParagraph).contains(factorPara)
    val sourceInstantCondition = sopToCondition(edgeLabel.dependentSoPPair)

    def findLinkingFactor(source: DiagnosedCondition, standardOfProof: StandardOfProof) = {
      val diagnosed = source

      // assume the standard of proof is the same as the target accepted condition

      val referringStandardOfProof = standardOfProof
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

    def checkTimeLimitOnDiagnosed(sourceDiagnosed : DiagnosedCondition, standardOfProof: StandardOfProof) : Boolean = {

      val linkingFactor = findLinkingFactor(sourceDiagnosed,standardOfProof).get
      val timeLimitRequirement = parsePeriodFromFactor(linkingFactor.linkingFactor.getText,edgeLabel.dependentSoPPair.getConditionName)
      timeLimitRequirement match
      {
        case Some(v) => targetConditionWithinPeriodFromSource(sourceInstantCondition.asInstanceOf[DiagnosedCondition],targetInstantCondition.asInstanceOf[AcceptedCondition],v)
        case None => true
      }
    }

    // todo: for now, just test this only, not time limit
    targetOccuredBeforeSource

  }

  def getInstantGraph(acceptedConditions: List[AcceptedCondition], diagnosedConditions: List[DiagnosedCondition],testEdgeTraverse : Boolean) : Graph[SoPPair,LDiEdge] = {
    val sopPairs = acceptedConditions.map(ac => ac.soPPair) ++ diagnosedConditions.map(dc => dc.soPPair)
    val graph: Graph[SoPPair, LDiEdge] = buildGraph(sopPairs)

    val edges = graph.edges.filter(e => {
      if (testEdgeTraverse)
        {
          canTraverse(e.toOuter.label.asInstanceOf[FactorRefForSoPPair],acceptedConditions,diagnosedConditions)
        }
      else
        true
    }).map(_.toOuter)
    val nodesInEdges = edges.flatMap(e => e.sources ++ e.targets)

    Graph.from(nodesInEdges,edges)
  }


  def findNodesInCycles(graph : Graph[SoPPair,LDiEdge])  = {
    graph.nodes
      .filter(n => graph.findCycleContaining(n).isDefined)
      .map(n => n.toOuter)
  }


  def canTraverseFromDiagnosedToAccepted(graph: Graph[SoPPair, LDiEdge], sourceCondition: SoPPair, acceptedConditions: Set[SoPPair]) = {
    // todo: get standard of proof from root condition and then check label to see if there is a link for that standard

    // get accepted nodes
    val acceptedNodes = graph.nodes.filter(n => acceptedConditions.contains(n.toOuter))
    val sourceNode = graph get sourceCondition
    val pathsToTarget: collection.Set[graph.Path] = acceptedNodes.map(n => sourceNode.pathTo(n)).filter(p => p.isDefined).map(p => p.get)

    pathsToTarget.nonEmpty

  }

  def getSvg(dto: SequelaeRequestDto, soPPairs : ImmutableSet[SoPPair])  = {
    val (accepted, diagnosed) = InstantConditions.decomposeRequestDto(dto,soPPairs)
    val graph : Graph[SoPPair, LDiEdge] =  getInstantGraph(accepted.toList, diagnosed.toList,true)
    val dotString = Dependencies.toDotString(graph)
    val os  = new ByteArrayOutputStream()
    val svgString = DotToImage.render(dotString,os)
    os.toByteArray
  }

  def getInferredSequelae(dto: SequelaeRequestDto, soPPairs : ImmutableSet[SoPPair]) : AcceptedSequalaeResponse = {
    val (accepted, diagnosed) = InstantConditions.decomposeRequestDto(dto,soPPairs)
    val graph : Graph[SoPPair, LDiEdge] =  getInstantGraph(accepted.toList, diagnosed.toList,true)
    val acceptedSoPPairs = accepted.map(c => c.soPPair).toSet
    val diagnosedSopPaairs = diagnosed.map(c => c.soPPair).toSet
    val sequelae = diagnosedSopPaairs.filter(n => canTraverseFromDiagnosedToAccepted(graph,n,acceptedSoPPairs))

    val diagnosedConditionNames = diagnosed.map(c => c.soPPair.getConditionName).toSet

    val edgesGoingFromDiagnosedConditions = graph.edges.filter(e => sequelae.map(s => s.getConditionName).contains(e.sources.head.toOuter.asInstanceOf[SoPPair].getConditionName))



    def factorRefToDto(ref : FactorRefForSoPPair) : AcceptedSequalaeResponseConditionDto   =
    {
      new AcceptedSequalaeResponseConditionDto(
        ref.dependentSoPPair.getConditionName,
        new FactorListDto(ref.dependentSoPPair.getRhSop.getRegisterId,ref.rhRefs.map(r => DtoTransformations.fromFactorToLink(r.linkingFactor)).asJava),
        new FactorListDto(ref.dependentSoPPair.getBopSop.getRegisterId,ref.bopRefs.map(r => DtoTransformations.fromFactorToLink(r.linkingFactor)).asJava)
        )
    }



    val inferredSequelaeDtos = edgesGoingFromDiagnosedConditions.map(e => {
      val label = e.label.asInstanceOf[FactorRefForSoPPair]
      val dto = factorRefToDto(label)
      dto
    })

    // remove nodes in cycles for the purpose of topo sorting
    val nodesInCycles = findNodesInCycles(graph)
    val graphWithCycleNodesRemoved = nodesInCycles.foldLeft(graph)((acc,n) => acc - n)

    val topoSortedConditions = graphWithCycleNodesRemoved.topologicalSort.fold(cycleNode => None, order => Some(order))
    val orderOfApplication = topoSortedConditions match {
      case None => List()
      case Some(o) => o.toList.map(n => n.toOuter.asInstanceOf[SoPPair].getConditionName).filter(n => diagnosedConditionNames.contains(n)).reverse
    }

    new AcceptedSequalaeResponse(inferredSequelaeDtos.toList.asJava, orderOfApplication.asJava)

  }

}

