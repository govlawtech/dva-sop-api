package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;

public interface RuleConfigurationItem {
    String getConditionName();

    String getInstrumentId();

    ImmutableSet<String> getFactorReferences();

    ServiceBranch getServiceBranch();

    Rank getRank();

    int getRequiredCFTSDays();


}



