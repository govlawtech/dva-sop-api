package au.gov.dva.sopapi.sopref.parsing.implementations.parsers

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.sopref.parsing.implementations.model.FactorInfo
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.FactorsParser.{MainPara, ParaLines}
import au.gov.dva.sopapi.sopref.parsing.traits.{PreAug2015FactorsParser, PreAugust2015SoPParser}

import scala.util.matching.Regex

object RenalStoneRHFactorsParser extends PreAugust2015SoPParser with PreAug2015FactorsParser {
  override def parseFactors(factorsSection: String): (StandardOfProof, List[FactorInfo]) = {

    val sectionsToSkip = Set("(i)(a)", "(b)", "(ii)")

    def customRuleForThirdLevelPara(mainPara: MainPara, paraLines: ParaLines) = {

      val applicable = mainPara.paraLinesParent.lines.head.startsWith("(ff)")
      val applies = sectionsToSkip.exists(toSkip => paraLines.lines.head.startsWith(toSkip))
      applicable && applies
    }

    parseFactorsSection(factorsSection,customRuleForThirdLevelPara)

  }
}
