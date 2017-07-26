package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.ApplicableSopResult;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;

import java.util.Optional;

public class ApplicableSopResultImpl implements ApplicableSopResult {

    private final Optional<SoP> applicableSoP;
    private final Interval intervalUsedToTestService;

    public ApplicableSopResultImpl(Optional<SoP> applicableSoP, Interval intervalUsedToTestOperationalService)
    {

        this.applicableSoP = applicableSoP;
        this.intervalUsedToTestService = intervalUsedToTestOperationalService;
    }

    @Override
    public Optional<SoP> getApplicableSop() {
        return applicableSoP;
    }

    @Override
    public Interval getIntervalUsedToTestOperationalService() {
        return intervalUsedToTestService;
    }
}
