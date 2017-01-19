package au.gov.dva.sopapi.sopref.parsing.implementations

import java.time.LocalDate
import java.util.Optional

import au.gov.dva.sopapi.interfaces.model._
import au.gov.dva.sopapi.dtos.StandardOfProof
import com.google.common.collect.{ImmutableList, ImmutableSet}

class ParsedSop(registerId: String, instrumentNumber: InstrumentNumber, citation: String, aggravationFactors: List[Factor],
                onsetFactors: List[Factor], effectiveFromDate : LocalDate, standardOfProof: StandardOfProof, icdCodes :  List[ICDCode], conditionName : String) extends SoP
{

  override def getRegisterId: String = registerId

  override def getInstrumentNumber: InstrumentNumber = instrumentNumber

  override def getCitation: String = citation

  override def getAggravationFactors: ImmutableList[Factor] = ImmutableList.copyOf(aggravationFactors.toArray)

  override def getOnsetFactors: ImmutableList[Factor] = ImmutableList.copyOf(onsetFactors.toArray)

  override def getEffectiveFromDate: LocalDate = effectiveFromDate

  override def getStandardOfProof: StandardOfProof = standardOfProof

  override def getICDCodes: ImmutableSet[ICDCode] = ImmutableSet.copyOf(icdCodes.toArray)

  override def getConditionName: String = conditionName

  override def getEndDate = Optional.empty()
}
