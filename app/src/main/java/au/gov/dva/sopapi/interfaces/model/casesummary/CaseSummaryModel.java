package au.gov.dva.sopapi.interfaces.model.casesummary;

import au.gov.dva.sopapi.interfaces.model.*;
import com.google.common.collect.ImmutableSet;

public interface CaseSummaryModel {
    Condition getCondition();
    ServiceHistory getServiceHistory();
    SoP getApplicableSop();
    String getThresholdProgress();
    ImmutableSet<Factor> getFactorsConnectedToService();
}
