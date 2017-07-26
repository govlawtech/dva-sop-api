package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;

import java.util.Optional;

public interface ApplicableSopResult {

    Optional<SoP> getApplicableSop();
    Interval getIntervalUsedToTestOperationalService();

}
