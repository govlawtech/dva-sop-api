package au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.interfaces.IntervalSelector;
import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.google.common.collect.ImmutableList;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// For factors which specify 'any' periods, such as lumbar spondylosis: 'any ten year period'.
public class SlidingOperationalServicePeriodSelector implements IntervalSelector {


    private Predicate<Deployment> isOperational;
    private int numberOfCalendarYearsInPeriod;

    public SlidingOperationalServicePeriodSelector(Predicate<Deployment> isOperational, int numberOfCalendarYearsInPeriod)
    {
        this.isOperational = isOperational;
        this.numberOfCalendarYearsInPeriod = numberOfCalendarYearsInPeriod;
    }

    @Override
    public Interval getInterval(ServiceHistory serviceHistory, LocalDate upperBoundaryInclusive) {

        ImmutableList<Deployment> operationalDeployments =
                ProcessingRuleFunctions.getCFTSDeployments(serviceHistory).stream()
                        .filter(d -> isOperational.test(d))
                        .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        LocalDate startDate = ProcessingRuleFunctions.getStartofService(serviceHistory).get();

        ImmutableList<Interval> intervalsWithMostOperationalService =  ProcessingRuleFunctions.getIntervalsWithMaximumService(numberOfCalendarYearsInPeriod,startDate,upperBoundaryInclusive,operationalDeployments);

        Comparator<Interval> mostCftsFirst = (o1, o2) -> Long.compare(getDaysOfCFTSInInterval(serviceHistory,o2),getDaysOfCFTSInInterval(serviceHistory,o1));
        Comparator<Interval> mostRecentFirst = (o1, o2) -> o2.getEnd().compareTo(o1.getEnd());

        List<Interval> intervalsWithMostOperationalServiceOrderedByCFTSThenRecency = intervalsWithMostOperationalService.stream()
                .sorted(mostCftsFirst.thenComparing(mostRecentFirst)).collect(Collectors.toList());

        return intervalsWithMostOperationalServiceOrderedByCFTSThenRecency.get(0);
    }



    private static long getDaysOfCFTSInInterval(ServiceHistory serviceHistory, Interval interval)
    {
        ImmutableList<Service> cftsServices = serviceHistory.getServices().stream()
                .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        return ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(interval.getStart(),interval.getEnd(),cftsServices);
    }


}


