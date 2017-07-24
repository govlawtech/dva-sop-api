package au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.interfaces.IntervalSelector;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.google.common.collect.ImmutableList;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SlidingCFTSIntervalSelector implements IntervalSelector {

   private int numberOfCalendarYearsInPeriod;

   public SlidingCFTSIntervalSelector(int numberOfCalendarYearsInPeriod)
   {
       this.numberOfCalendarYearsInPeriod = numberOfCalendarYearsInPeriod;
   }


    @Override
    public Interval getInterval(ServiceHistory serviceHistory, LocalDate upperBoundaryInclusive) {

        ImmutableList<Service> cftsServices = ProcessingRuleFunctions.getCFTSServices(serviceHistory);

        ImmutableList<Interval> intervalsWithMostCftsService =  ProcessingRuleFunctions.getIntervalsWithMaximumService(numberOfCalendarYearsInPeriod,serviceHistory.getStartofService().get(),upperBoundaryInclusive,cftsServices);

        Comparator<Interval> mostCftsFirst = (o1, o2) -> Long.compare(getDaysOfCFTSInInterval(serviceHistory,o2),getDaysOfCFTSInInterval(serviceHistory,o1));
        Comparator<Interval> mostRecentFirst = (o1, o2) -> o2.getEnd().compareTo(o1.getEnd());

        Stream<Interval> intervalsWithMostOperationalServiceOrderedByCFTSThenRecency = intervalsWithMostCftsService.stream()
                .sorted(mostCftsFirst.thenComparing(mostRecentFirst));

        return intervalsWithMostOperationalServiceOrderedByCFTSThenRecency.findFirst().get();
    }


    protected static long getDaysOfCFTSInInterval(ServiceHistory serviceHistory, Interval interval)
    {
        ImmutableList<Service> cftsServices = serviceHistory.getServices().stream()
                .filter(s -> s.getEmploymentType() == EmploymentType.CFTS)
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        return ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(interval.getStart(),interval.getEnd(),cftsServices);
    }
}

