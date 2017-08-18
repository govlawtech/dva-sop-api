package au.gov.dva.sopapi.sopref.parsing.traits

import java.time.LocalDate

import au.gov.dva.sopapi.dtos.StandardOfProof
import au.gov.dva.sopapi.exceptions.SopParserRuntimeException
import au.gov.dva.sopapi.interfaces.model.{DefinedTerm, Factor, ICDCode, SoP}
import au.gov.dva.sopapi.sopref.parsing.implementations.model.{FactorInfo, ParsedFactor, ParsedSop}
import au.gov.dva.sopapi.sopref.parsing.implementations.parsers.PreAugust2015Parser


trait SoPFactory extends MiscRegexes {
  def create(registerId: String, cleansedText: String): SoP

  def create(registerId: String, cleansedText: String, extractor: SoPExtractor, parser: SoPParser, allocateFactors : List[Factor] => (List[Factor],List[Factor])): ParsedSop = {
    val citation = parser.parseCitation(extractor.extractCitation(cleansedText))
    val instrumentNumber = parser.parseInstrumentNumber(citation)

    val definedTermsList: List[DefinedTerm] = parser.parseDefinitions(extractor.extractDefinitionsSection(cleansedText))

    val (factorsSectionNumber, factorsSectionText): (Int, String) = extractor.extractFactorsSection(cleansedText)


    val (standard, factorInfos): (StandardOfProof, List[FactorInfo]) = parser.parseFactors(factorsSectionText)

    val factorObjects: List[Factor] = this.buildFactorObjectsFromInfo(factorInfos, factorsSectionNumber, definedTermsList)

    val (onsetFactors, aggravationFactors) =  allocateFactors(factorObjects)

    val effectiveFromDate: LocalDate = parser.parseDateOfEffect(extractor.extractDateOfEffectSection(cleansedText))

    val icdCodes: List[ICDCode] = extractor.extractICDCodes(cleansedText)

    val conditionName = parser.parseConditionNameFromCitation(citation);

    new ParsedSop(registerId, instrumentNumber, citation, aggravationFactors, onsetFactors, effectiveFromDate, standard, icdCodes, conditionName)
  }

  def create(registerId: String, cleansedText: String, extractor: SoPExtractor, parser: SoPParser): ParsedSop = {


    val aggravationSection = extractor.extractAggravationSection(cleansedText)
    val (_, factorsSectionText): (Int, String) = extractor.extractFactorsSection(cleansedText)

    def defaultFactorAllocator(factorObjects : List[Factor]) : (List[Factor], List[Factor]) = {
      val (onsetFactors, aggravationFactors) = (aggravationSection.isDefined) match {
        case true => allocateFactorsToOnsetAndAggravationBasedOnAggravationSection(factorObjects, aggravationSection.get, parser.parseStartAndEndAggravationParas)
        case false => allocateFactorsToOnsetAndAggravationBasedOnFactorsSection(factorObjects,factorsSectionText)
      }
      (onsetFactors,aggravationFactors)
    }

    this.create(registerId,cleansedText,extractor,parser,defaultFactorAllocator)

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
      .map(fi => (factorSectionNumber.toString.concat(fi.getLetter), fi.getText))
      .map(i => {

        val relevantDefinitions = definedTerms.filter(d => i._2.contains(d.getTerm)).toSet
        new ParsedFactor(i._1, i._2, relevantDefinitions)
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


  private def allocateFactorsToOnsetAndAggravationBasedOnFactorsSection(factorObjects: List[Factor], factorsSection: String) : (List[Factor],List[Factor]) = {
    val flattenedFactorsSectionText = factorsSection.replaceAll(platformNeutralLineEndingRegex.regex," ")
    if  (factorObjects.size != 1) throw new SopParserRuntimeException("Expected only one factor object.")
    if (!factorsSection.startsWith("The factor that must")) throw new SopParserRuntimeException("Factor section does not looks like a section for a single factor.")
    val textIndicatingFactorIsBothOnsetAndAggravation = "causing or materially contributing to or aggravating"
    if (flattenedFactorsSectionText.contains(textIndicatingFactorIsBothOnsetAndAggravation)) {
       return (factorObjects,factorObjects)
    }
    else return (factorObjects,List())
  }


}