package au.gov.dva.sopapi;

import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoP;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;

class Cache {

    private ImmutableSet<SoP> _allSops;
    private ImmutableSet<ServiceDetermination> _allServiceDeterminations;


    private static final Cache INSTANCE = new Cache();

    private Cache() {
        _allSops = ImmutableSet.of();
        _allServiceDeterminations = ImmutableSet.of();
    }

    public static Cache getInstance() {
        return INSTANCE;
    }

    public void refresh(Repository repository)
    {

        ImmutableSet<SoP> allSops = repository.getAllSops();
        ImmutableSet<ServiceDetermination> allServiceDeterminations = repository.getServiceDeterminations();

        // atomic
        _allSops = allSops;
        _allServiceDeterminations = allServiceDeterminations;

    }

    public ImmutableSet<SoP> get_allSops() {
        return _allSops;
    }

    public ImmutableSet<ServiceDetermination> get_allServiceDeterminations() {
        return _allServiceDeterminations;
    }


}
