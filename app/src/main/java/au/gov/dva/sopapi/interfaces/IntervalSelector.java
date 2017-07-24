package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;

import java.time.LocalDate;

// Select the interval for testing RH and CFTS
public interface IntervalSelector {
    Interval getInterval(ServiceHistory serviceHistory, LocalDate upperBoundaryInclusive);
}
