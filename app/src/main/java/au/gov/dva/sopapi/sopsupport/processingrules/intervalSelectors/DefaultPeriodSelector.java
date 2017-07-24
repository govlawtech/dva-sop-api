package au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors;

import au.gov.dva.sopapi.interfaces.IntervalSelector;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;

import java.time.LocalDate;

public class DefaultPeriodSelector implements IntervalSelector {

    @Override
    public Interval getInterval(ServiceHistory serviceHistory, LocalDate upperBoundaryInclusive) {

        return new Interval(serviceHistory.getStartofService().get(), upperBoundaryInclusive);
    }
}

