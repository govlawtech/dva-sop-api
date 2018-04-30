package au.gov.dva.sopapi;

import au.gov.dva.sopapi.dtos.sopref.ConditionInfo;
import au.gov.dva.sopapi.dtos.sopref.ICDCodeDto;
import au.gov.dva.sopapi.interfaces.Cache;
import au.gov.dva.sopapi.interfaces.CuratedTextRepository;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.SoPs;
import au.gov.dva.sopapi.veaops.VeaDetermination;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CacheSingleton implements Cache {

    private static Logger logger = LoggerFactory.getLogger("dvasopapi.repositorycache");

    private ImmutableSet<SoP> _allSops;
    private ImmutableSet<SoPPair> _allSopPairs;
    private ImmutableSet<ServiceDetermination> _allMrcaServiceDeterminations;
    private RuleConfigurationRepository _ruleConfigurationRepository;
    private ImmutableSet<InstrumentChange> _failedUpdates;
    private ImmutableList<ConditionInfo> _conditionList;
    private Optional<CuratedTextRepository> _curatedTextReporitory;
    private ImmutableSet<VeaDetermination> _veaDeterminations;

    private static final CacheSingleton INSTANCE = new CacheSingleton();

    private CacheSingleton() {
        _allSops = ImmutableSet.of();
        _allSopPairs = ImmutableSet.of();
        _allMrcaServiceDeterminations = ImmutableSet.of();
        _failedUpdates = ImmutableSet.of();
        _conditionList = ImmutableList.of();
        _curatedTextReporitory = Optional.empty();
        _veaDeterminations = ImmutableSet.of();
    }

    public static CacheSingleton getInstance() {
        return INSTANCE;
    }



    @Override
    public void refresh(Repository repository)
    {
        try {
            ImmutableSet<SoP> allSops = repository.getAllSops();
            ImmutableSet<ServiceDetermination> allServiceDeterminations = repository.getServiceDeterminations();
            Optional<RuleConfigurationRepository> ruleConfigurationRepository = repository.getRuleConfigurationRepository();
            if (!ruleConfigurationRepository.isPresent()) {
                throw new ConfigurationRuntimeException("Need rules configuration to be repository.");
            }
            ImmutableSet<InstrumentChange> failed = repository.getRetryQueue();
            ImmutableSet<SoPPair> soPPairs = SoPs.groupSopsToPairs(allSops, OffsetDateTime.now());
            ImmutableList<ConditionInfo> conditionsList = ImmutableList.copyOf(buildConditionsList(soPPairs));
            Optional<CuratedTextRepository> curatedTextRepository = repository.getCuratedTextRepository();
            ImmutableSet<VeaDetermination> veaDeterminations = repository.getVeaDeterminations();


            _allSops = allSops;
            _allSopPairs = soPPairs;
            _allMrcaServiceDeterminations = allServiceDeterminations;
            _ruleConfigurationRepository = ruleConfigurationRepository.get();
            _failedUpdates = failed;
            _conditionList = conditionsList;
            _curatedTextReporitory = curatedTextRepository;
            _veaDeterminations = veaDeterminations;
        } catch (Exception e) {
            logger.error("Exception occurred when attempting to refresh cache from Repository.", e);
        } catch (Error e) {
            logger.error("Error occurred when attempting to refresh cache from Repository.", e);
        }

    }

    @Override
    public ImmutableList<ConditionInfo> get_conditionsList() {
        return _conditionList;
    }

    @Override
    public ImmutableSet<SoP> get_allSops() {
        return _allSops;
    }

    @Override
    public ImmutableSet<SoPPair> get_allSopPairs() {
        return _allSopPairs;
    }

    @Override
    public ImmutableSet<ServiceDetermination> get_allMrcaServiceDeterminations() {
        return _allMrcaServiceDeterminations;
    }

    @Override
    public RuleConfigurationRepository get_ruleConfigurationRepository() {
        return _ruleConfigurationRepository;
    }



    public ImmutableSet<InstrumentChange> get_failedUpdates() {
        return _failedUpdates;
    }

    private List<ConditionInfo> buildConditionsList(ImmutableSet<SoPPair> soPPairs) {
        return soPPairs.stream()
                .map(soPPair -> new ConditionInfo(
                        soPPair.getConditionName(),
                        soPPair.getRhSop().getRegisterId(),
                        soPPair.getBopSop().getRegisterId(),
                        soPPair.getICDCodes()
                                .stream()
                                .map(icdCode -> new ICDCodeDto(icdCode.getVersion(), icdCode.getCode()))
                                .collect(Collectors.toList())
                ))
                .sorted(Comparator.comparing(ConditionInfo::get_conditionName))
                .collect(Collectors.toList());

    }

    @Override
    public Optional<CuratedTextRepository> get_curatedTextReporitory() {
        return _curatedTextReporitory;
    }
}

