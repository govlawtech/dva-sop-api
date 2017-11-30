package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import com.google.common.collect.ImmutableSet;

public interface Cache {
    void refresh(Repository repository);

    ImmutableSet<String> get_conditionNames();

    ImmutableSet<SoP> get_allSops();

    ImmutableSet<SoPPair> get_allSopPairs();

    ImmutableSet<ServiceDetermination> get_allServiceDeterminations();

    RuleConfigurationRepository get_ruleConfigurationRepository();
}
