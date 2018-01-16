package au.gov.dva.sopapi;

import au.gov.dva.sopapi.interfaces.Cache;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopref.SoPs;
import au.gov.dva.sopapi.sopsupport.vea.ServiceRegion;
import au.gov.dva.sopapi.sopsupport.vea.SingleOnlineClaimFormOpImpl;
import au.gov.dva.sopapi.sopsupport.vea.SingleOnlineClaimFormVeaOps;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private ImmutableList<ServiceRegion> _serviceRegions;

    private static final CacheSingleton INSTANCE = new CacheSingleton();

    private CacheSingleton() {
        _allSops = ImmutableSet.of();
        _allSopPairs = ImmutableSet.of();
        _allServiceDeterminations = ImmutableSet.of();
        _failedUpdates = ImmutableSet.of();
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

            Optional<String> socfYaml = repository.getSocfServiceRegionsYaml();
            if (!socfYaml.isPresent())
            {
                throw new ConfigurationRuntimeException("Need Single Online Claim Form VEA operations to be in repository.");
            }

            ImmutableList<ServiceRegion> serviceRegions = SingleOnlineClaimFormVeaOps.fromYaml(socfYaml.get()).stream()
                    .map(SingleOnlineClaimFormOpImpl::toServiceRegion)
                    .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));

            // atomic
            _allSops = allSops;
            _allSopPairs = SoPs.groupSopsToPairs(_allSops, OffsetDateTime.now());
            _allServiceDeterminations = allServiceDeterminations;
            _ruleConfigurationRepository = ruleConfigurationRepository.get();
            _failedUpdates = failed;
            _serviceRegions = serviceRegions;
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

    @Override
    public ImmutableList<ServiceRegion> getVeaSocfServiceRegions() {
        return null;
    }

    public ImmutableSet<InstrumentChange> get_failedUpdates() {
        return _failedUpdates;
    }
}

