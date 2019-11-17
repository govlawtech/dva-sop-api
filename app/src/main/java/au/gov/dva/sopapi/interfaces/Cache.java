package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.sopref.ConditionInfo;
import au.gov.dva.sopapi.dtos.sopref.ConditionsList;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopref.dependencies.Dependencies;
import au.gov.dva.sopapi.veaops.interfaces.VeaOperationalServiceRepository;
import com.google.common.collect.ImmutableList;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopsupport.vea.ServiceRegion;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;

public interface Cache {
    void refresh(Repository repository);

    ImmutableList<ConditionInfo> get_conditionsList();

    ImmutableSet<SoP> get_allSops();

    ImmutableSet<SoPPair> get_allSopPairs();

    ImmutableSet<ServiceDetermination> get_allMrcaServiceDeterminations();

    RuleConfigurationRepository get_ruleConfigurationRepository();

    Optional<CuratedTextRepository> get_curatedTextReporitory();

    VeaOperationalServiceRepository get_veaOperationalServiceRepository();

    Dependencies get_dependencies();
}
