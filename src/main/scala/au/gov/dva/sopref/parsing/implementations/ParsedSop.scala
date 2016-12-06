package au.gov.dva.sopref.parsing.implementations

import java.time.LocalDate

import au.gov.dva.sopref.interfaces.model.{Factor, InstrumentNumber, SoP, StandardOfProof}
import com.google.common.collect.ImmutableSet

class ParsedSop(registerId: String, instrumentNumber: InstrumentNumber, citation: String, aggravationFactors: Set[Factor],
                onsetFactors: Set[Factor], effectiveFromDate : LocalDate, standardOfProof: StandardOfProof) extends SoP
{

  override def getRegisterId: String = registerId

  override def getInstrumentNumber: InstrumentNumber = instrumentNumber

  override def getCitation: String = citation

  override def getAggravationFactors: ImmutableSet[Factor] = ImmutableSet.copyOf(aggravationFactors.toArray)

  override def getOnsetFactors: ImmutableSet[Factor] = ImmutableSet.copyOf(onsetFactors.toArray)

  override def getEffectiveFromDate: LocalDate = effectiveFromDate

  override def getStandardOfProof: StandardOfProof = standardOfProof
}
