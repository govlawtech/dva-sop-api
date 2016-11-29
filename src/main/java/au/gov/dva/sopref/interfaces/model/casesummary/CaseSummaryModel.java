package au.gov.dva.sopref.interfaces.model.casesummary;

import au.gov.dva.sopref.interfaces.model.*;
import com.google.common.collect.ImmutableSet;

public interface CaseSummaryModel {
    Condition getCondition();
    ServiceHistory getServiceHistory();
    SoP getSop();
    String getThresholdProgress();
    ImmutableSet<Factor> getFactorsConnectedToService();
}
