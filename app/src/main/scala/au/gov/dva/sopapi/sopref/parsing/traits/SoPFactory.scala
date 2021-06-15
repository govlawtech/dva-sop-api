package au.gov.dva.sopapi.sopref.parsing.traits

import java.time.LocalDate
import java.util.stream.{Collector, StreamSupport}

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.exceptions
import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, Factor, ICDCode, SoP}
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, ParsedFactor, ParsedFactorWithConditionVariant, ParsedSop}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PreAugust2015Parser
import com.google.common.collect.{ImmutableList, ImmutableSet, Iterables, Sets}

import scala.collection.JavaConverters._


trait SoPFactory extends MiscRegexes {
  def create(registerId: String, cleansedText: String): SoP

  def create(registerId: String, cleansedText: String, extractor: SoPExtractor, parser: SoPParser, allocateFactors: List[Factor] => (List[Factor], List[Factor])): ParsedSop = {
    val citation = parser.parseCitation(extractor.extractCitation(cleansedText))
    val instrumentNumber = parser.parseInstrumentNumber(citation)

    val definedTermsList: List[DefinedTerm] = parser.parseDefinitions(extractor.extractDefinitionsSection(cleansedText))

    val (factorsSectionNumber, factorsSectionText): (Int, String) = extractor.extractFactorsSection(cleansedText)

    val (standard, factorInfos): (StandardOfProof, List[FactorInfo]) = parser.parseFactors(factorsSectionText)

    val factorObjects: List[Factor] = this.buildFactorObjectsFromInfo(factorInfos, factorsSectionNumber, definedTermsList)

    val (onsetFactors, aggravationFactors) = allocateFactors(factorObjects)

    val effectiveFromDate: LocalDate = parser.parseDateOfEffect(extractor.extractDateOfEffectSection(cleansedText))

    val icdCodes: List[ICDCode] = extractor.extractICDCodes(cleansedText)

    val conditionName = parser.parseConditionNameFromCitation(citation);

    val sop = new ParsedSop(registerId, instrumentNumber, citation, aggravationFactors, onsetFactors, effectiveFromDate, standard, icdCodes, conditionName)
    executeRuntimeChecks(sop)
    sop
  }

  def create(registerId: String, cleansedText: String, extractor: SoPExtractor, parser: SoPParser): ParsedSop = {


    val aggravationSection = extractor.extractAggravationSection(cleansedText)
    val (_, factorsSectionText): (Int, String) = extractor.extractFactorsSection(cleansedText)

    def defaultFactorAllocator(factorObjects: List[Factor]): (List[Factor], List[Factor]) = {
      val (onsetFactors, aggravationFactors) = (aggravationSection.isDefined) match {
        case true => allocateFactorsToOnsetAndAggravationBasedOnAggravationSection(factorObjects, aggravationSection.get, parser.parseStartAndEndAggravationParas)
        case false => allocateFactorsToOnsetAndAggravationBasedOnFactorsSection(factorObjects, factorsSectionText)
      }
      (onsetFactors, aggravationFactors)
    }

    this.create(registerId, cleansedText, extractor, parser, defaultFactorAllocator)

  }

  def stripParaNumber(paraWithNumber: String): String = {
    assert(!paraWithNumber.takeWhile(c => c.isDigit).isEmpty)
    paraWithNumber.dropWhile(c => c.isDigit)
  }

  def splitFactors(parasInOrder: List[String], startPara: String, endPara: String): (List[String], List[String]) = {
    val firstChunkOfOnsetParas = parasInOrder.takeWhile(i => i != startPara);
    val lastChunkOfOnsetParas = parasInOrder.reverse.takeWhile(i => i != endPara).reverse;
    val allOnsetParas = firstChunkOfOnsetParas ++ lastChunkOfOnsetParas

    val aggParas = parasInOrder.filter(p => !allOnsetParas.contains(p))
    (allOnsetParas, aggParas)
  }

  def buildFactorObjectsFromInfo(factors: List[FactorInfo], factorSectionNumber: Int, definedTerms: List[DefinedTerm]): List[Factor] = {

    factors
      .map(factorInfo => {
        val paraLetterIncludingMainSectionRef = factorSectionNumber.toString.concat(factorInfo.getLetter)
        val relevantDefinitions = definedTerms.filter(d => factorInfo.getText.contains(d.getTerm)).toSet

        val parsedFactor = new ParsedFactor(paraLetterIncludingMainSectionRef, factorInfo.getText, relevantDefinitions)
        if (factorInfo.getConditionVariant.isEmpty) {
          parsedFactor
        }
        else {
          new ParsedFactorWithConditionVariant(parsedFactor,factorInfo.getConditionVariant.get)
        }

      })
  }


  private def allocateFactorsToOnsetAndAggravationBasedOnAggravationSection(factorObjects: List[Factor], aggravationSection: String,
                                                                            functionToIdentifyStartAndEndAggravationParasFromSection: (String => (String, String))): (List[Factor], List[Factor]) = {

    def divideFactorObjectsToOnsetAndAggravation(factorObjects: List[Factor], startParaLetterOfAgg: String, endParaLetterOfAgg: String): (List[Factor], List[Factor]) = {

      val orderedParaLetters = factorObjects.map(f => f.getParagraph.dropWhile(c => c.isDigit)).toList

      val splitOfOnsetAndAggravationFactors = this.splitFactors(
        orderedParaLetters, startParaLetterOfAgg, endParaLetterOfAgg)

      val onsetParasWithoutNumber = splitOfOnsetAndAggravationFactors._1;
      val aggParasWithoutNumber = splitOfOnsetAndAggravationFactors._2;

      val onsetFactors = factorObjects
        .filter(f => onsetParasWithoutNumber.contains(stripParaNumber(f.getParagraph)))
      val aggravationFactors = factorObjects
        .filter(f => aggParasWithoutNumber.contains(stripParaNumber(f.getParagraph)))

      (onsetFactors, aggravationFactors)

    }

    val (onsetFactors, aggravationFactors) = {
      if (factorObjects.size > 1) {
        val startAndEndOfAggravationParas = functionToIdentifyStartAndEndAggravationParasFromSection(aggravationSection)
        divideFactorObjectsToOnsetAndAggravation(factorObjects, startAndEndOfAggravationParas._1, startAndEndOfAggravationParas._2)
      }
      else if (factorObjects.size == 1) {
        val startAndEndOfAggravationParas = functionToIdentifyStartAndEndAggravationParasFromSection(aggravationSection)
        if (startAndEndOfAggravationParas._1 != startAndEndOfAggravationParas._2) throw new SopParserRuntimeException("Start paras of aggravation section should be the same if there is only one factor.")
        (List(), factorObjects)
      }

      else (factorObjects, List())
    }

    (onsetFactors, aggravationFactors)

  }


  private def allocateFactorsToOnsetAndAggravationBasedOnFactorsSection(factorObjects: List[Factor], factorsSection: String): (List[Factor], List[Factor]) = {
    val flattenedFactorsSectionText = factorsSection.replaceAll(platformNeutralLineEndingRegex.regex, " ")
    // if  (factorObjects.size != 1) throw new SopParserRuntimeException("Expected only one factor object.")
    if (!factorsSection.startsWith("The factor that must")) throw new SopParserRuntimeException("Factor section does not looks like a section for a single factor.")
    val textIndicatingFactorIsBothOnsetAndAggravation = "causing or materially contributing to or aggravating"
    if (flattenedFactorsSectionText.contains(textIndicatingFactorIsBothOnsetAndAggravation)) {
      return (factorObjects, factorObjects)
    }
    else return (factorObjects, List())
  }

  private val suspiciousParaRegex = """\([a-zA-Z0-9]+\)""".r

  def executeRuntimeChecks(sop: SoP) = {

    val allFactors = ImmutableList.builder[Factor].addAll(sop.getOnsetFactors).addAll(sop.getAggravationFactors).build()
    allFactors.forEach(f => {
      if (f.getText.contains("Veterans' Entitlements Act 1986") || f.getText.contains("Statement of Principles")) {
        throw new SopParserRuntimeException(sop.getRegisterId + ": factor likely contains erroneous text: " + f.getParagraph + ": " + f.getText)
      }

      val whiteListOfInstrumentsThatCanContainNotes = Set("F2018C00671","F2018C00670","F2020C00857","F2020C00856","F2021C00081","F2021C00080", "F2021C00459", "F2021C00460")
      if (f.getText.contains("Note: ") && !whiteListOfInstrumentsThatCanContainNotes.contains(sop.getRegisterId)) throw new SopParserRuntimeException("Factor text contains notes.")

      val paraMatches = suspiciousParaRegex.findAllMatchIn(f.getParagraph).size
      if (paraMatches > 1) {
        throw new SopParserRuntimeException("Suspicious paragraph reference: " + f.getParagraph + " : " + f.getText)
      }

      if (f.getText.startsWith("(")) throw new SopParserRuntimeException("Suspicious factor text: starts with open paren: " + f.getText)


    })

    val factorCount = allFactors.size()
    val uniqueRefsCount = allFactors.asScala.map(f => f.getParagraph).distinct.size
    // Hepatitis B has one factor which is half aggravation, half onset
    if (sop.getRegisterId != "F2017L00001") {

      if (uniqueRefsCount < factorCount && factorCount > 2) throw new SopParserRuntimeException(sop.getRegisterId + ": likely incorrectly parsed sub paras.")

      if (sop.getOnsetFactors.size() > 1) {

        sop.getOnsetFactors.forEach(f => {
          if (f.getText.contains("inability to obtain appropriate clinical") || f.getText.contains("clinical worsening")) {
            throw new SopParserRuntimeException(sop.getRegisterId + ": aggravation factor likely mislabelled as onset factor: " + f.getParagraph + ": " + f.getText)

          }
        })
      }


    }


  }

}

