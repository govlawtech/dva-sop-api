package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.model.FactorReference;
import com.google.common.collect.ImmutableSet;

import java.util.stream.Collectors;

public interface RuleConfigurationItem {
    String getConditionName();

    String getInstrumentId();

    ImmutableSet<FactorReference> getFactorRefObjects();

    ServiceBranch getServiceBranch();

    Rank getRank();

    int getRequiredCFTSDays();

    default ImmutableSet<String> getMainFactorReferences() {
        ImmutableSet<String> mainParts = getFactorRefObjects().stream()
                .map(f -> f.getMainFactorReference())
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ImmutableSet::copyOf));
        return mainParts;

    }

}



