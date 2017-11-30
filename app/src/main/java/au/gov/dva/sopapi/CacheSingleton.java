package au.gov.dva.sopapi;

import au.gov.dva.sopapi.interfaces.Cache;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopref.SoPs;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.math.Ordering;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

public class CacheSingleton implements Cache {

    private static Logger logger = LoggerFactory.getLogger("dvasopapi.repositorycache");

    private ImmutableSet<SoP> _allSops;
    private ImmutableSet<SoPPair> _allSopPairs;
    private ImmutableSet<ServiceDetermination> _allServiceDeterminations;
    private RuleConfigurationRepository _ruleConfigurationRepository;
    private ImmutableSet<InstrumentChange> _failedUpdates;
    private ImmutableSet<String> _conditionNames;

    private static final CacheSingleton INSTANCE = new CacheSingleton();

    private CacheSingleton() {
        _allSops = ImmutableSet.of();
        _allSopPairs = ImmutableSet.of();
        _allServiceDeterminations = ImmutableSet.of();
        _failedUpdates = ImmutableSet.of();
        _conditionNames = ImmutableSet.of();
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
            ImmutableSet<SoPPair> soPPairs = SoPs.groupSopsToPairs(allSops,OffsetDateTime.now());
            ImmutableSet<String> conditionNames = soPPairs.stream().map(soPPair -> soPPair.getConditionName()).collect(Collectors.collectingAndThen(Collectors.toSet(),ImmutableSet::copyOf));

            // atomic
            _allSops = allSops;
            _allSopPairs = soPPairs;
            _allServiceDeterminations = allServiceDeterminations;
            _ruleConfigurationRepository = ruleConfigurationRepository.get();
            _failedUpdates = failed;
            _conditionNames = conditionNames;
        }
        catch (Exception e)
        {
            logger.error("Exception occurred when attempting to refresh cache from Repository.", e);
        }

        catch (Error e)
        {
            logger.error("Error occurred when attempting to refresh cache from Repository.", e);
        }

    }

    @Override
    public ImmutableSet<String> get_conditionNames() {
        return _conditionNames;
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
    public ImmutableSet<ServiceDetermination> get_allServiceDeterminations() {
        return _allServiceDeterminations;
    }

    @Override
    public RuleConfigurationRepository get_ruleConfigurationRepository() {
        return _ruleConfigurationRepository;
    }

    public ImmutableSet<InstrumentChange> get_failedUpdates() {
        return _failedUpdates;
    }
}

