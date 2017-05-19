package au.gov.dva.sopapi;

import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopref.SoPs;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Cache {

    private static Logger logger = LoggerFactory.getLogger("dvasopapi.repositorycache");

    private ImmutableSet<SoP> _allSops;
    private ImmutableSet<SoPPair> _allSopPairs;
    private ImmutableSet<ServiceDetermination> _allServiceDeterminations;
    private RuleConfigurationRepository _ruleConfigurationRepository;

    private static final Cache INSTANCE = new Cache();

    private Cache() {
        _allSops = ImmutableSet.of();
        _allSopPairs = ImmutableSet.of();
        _allServiceDeterminations = ImmutableSet.of();

    }

    public static Cache getInstance() {
        return INSTANCE;
    }

    public void refresh(Repository repository)
    {
        try {

            ImmutableSet<SoP> allSops = repository.getAllSops();
            ImmutableSet<ServiceDetermination> allServiceDeterminations = repository.getServiceDeterminations();
            Optional<RuleConfigurationRepository> ruleConfigurationRepository = repository.getRuleConfigurationRepository();
            if (!ruleConfigurationRepository.isPresent()) {
                throw new ConfigurationError("Need rules configuration to be repository.");
            }

            // atomic
            _allSops = allSops;
            _allSopPairs = SoPs.groupSopsToPairs(_allSops);
            _allServiceDeterminations = allServiceDeterminations;
            _ruleConfigurationRepository = ruleConfigurationRepository.get();
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

    public ImmutableSet<SoP> get_allSops() {
        return _allSops;
    }

    public ImmutableSet<SoPPair> get_allSopPairs() {
        return _allSopPairs;
    }

    public ImmutableSet<ServiceDetermination> get_allServiceDeterminations() {
        return _allServiceDeterminations;
    }

    public RuleConfigurationRepository get_ruleConfigurationRepository() {
        return _ruleConfigurationRepository;
    }
}

