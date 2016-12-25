package au.gov.dva.sopapi.sopref.data.servicedeterminations;

import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import com.google.common.collect.ImmutableSet;

public class ServiceDeterminationPair {

    private final ServiceDetermination nonWarlike;
    private final ServiceDetermination warlike;

    public ServiceDeterminationPair(ServiceDetermination warlike, ServiceDetermination nonWarlike) {
        this.warlike = warlike;
        this.nonWarlike = nonWarlike;
    }

    public ImmutableSet<ServiceDetermination> getBoth() {
        return ImmutableSet.of(warlike,nonWarlike);
    }

    public ServiceDetermination getNonWarlike() {
        return nonWarlike;
    }

    public ServiceDetermination getWarlike() {
        return warlike;
    }
}
