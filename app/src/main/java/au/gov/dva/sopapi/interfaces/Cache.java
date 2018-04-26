package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.sopref.ConditionInfo;
import au.gov.dva.sopapi.dtos.sopref.ConditionsList;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import com.google.common.collect.ImmutableList;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;

public interface Cache {
    void refresh(Repository repository);

    ImmutableList<ConditionInfo> get_conditionsList();

    ImmutableSet<SoP> get_allSops();

    ImmutableSet<SoPPair> get_allSopPairs();

    ImmutableSet<ServiceDetermination> get_allServiceDeterminations();

    RuleConfigurationRepository get_ruleConfigurationRepository();

    Optional<CuratedTextRepository> get_curatedTextReporitory();
}
