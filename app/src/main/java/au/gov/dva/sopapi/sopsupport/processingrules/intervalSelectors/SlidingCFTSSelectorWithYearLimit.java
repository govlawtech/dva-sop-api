package au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors;

import au.gov.dva.sopapi.interfaces.IntervalSelector;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.stream.Stream;

public class SlidingCFTSSelectorWithYearLimit extends SlidingCFTSIntervalSelector
{

    private final int numberOfYearsInSlidingPeriod;
    private final int yearLimit;

    public SlidingCFTSSelectorWithYearLimit(int numberOfYearsInSlidingPeriod, int yearLimit)
    {
        super(numberOfYearsInSlidingPeriod);

        this.numberOfYearsInSlidingPeriod = numberOfYearsInSlidingPeriod;
        this.yearLimit = yearLimit;
    }

    @Override
    public Interval getInterval(ServiceHistory serviceHistory, LocalDate upperBoundaryInclusive) {

        ImmutableList<Service> cftsServices = ProcessingRuleFunctions.getCFTSServices(serviceHistory);

        LocalDate lowerBoundary = upperBoundaryInclusive.minusYears(numberOfYearsInSlidingPeriod + yearLimit); // "where the clinical onset occurs within 25 years _following_ that period
        if (serviceHistory.getStartofService().get().isAfter(lowerBoundary))
            lowerBoundary = serviceHistory.getStartofService().get();

        ImmutableList<Interval> intervalsWithMostCftsService =  ProcessingRuleFunctions.getIntervalsWithMaximumService(numberOfYearsInSlidingPeriod, lowerBoundary, upperBoundaryInclusive,cftsServices);

        Comparator<Interval> mostCftsFirst = (o1, o2) -> Long.compare(getDaysOfCFTSInInterval(serviceHistory,o2),getDaysOfCFTSInInterval(serviceHistory,o1));
        Comparator<Interval> mostRecentFirst = (o1, o2) -> o2.getEnd().compareTo(o1.getEnd());

        Stream<Interval> intervalsWithMostOperationalServiceOrderedByCFTSThenRecency = intervalsWithMostCftsService.stream()
                .sorted(mostCftsFirst.thenComparing(mostRecentFirst));

        return intervalsWithMostOperationalServiceOrderedByCFTSThenRecency.findFirst().get();
    }
}
