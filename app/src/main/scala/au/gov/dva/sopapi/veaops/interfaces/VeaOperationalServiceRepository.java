package au.gov.dva.sopapi.veaops.interfaces;

import au.gov.dva.sopapi.veaops.VeaDetermination;
import au.gov.dva.sopapi.veaops.VeaPeacekeepingActivity;
import com.google.common.collect.ImmutableSet;

public interface VeaOperationalServiceRepository {
    ImmutableSet<VeaDetermination> getDeterminations();
    ImmutableSet<VeaPeacekeepingActivity> getPeacekeepingActivities();

}
