package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.OnsetCondition;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by mc on 27/06/17.
 */
public class ProcessingRuleFunctionsTest {

    private Deployment makeDeployment(int beginYear, int beginMonth, int beginDay, int endYear, int endMonth, int endDay) {
        return new DeploymentImpl(""
                , LocalDate.of(beginYear, beginMonth, beginDay)
                , Optional.of(LocalDate.of(endYear, endMonth, endDay))
                ,"Within Specified Area"
        );
    }

    private Deployment makeDeployment(int beginYear, int beginMonth, int beginDay) {
        return new DeploymentImpl(""
                , LocalDate.of(beginYear, beginMonth, beginDay)
                , Optional.empty()
                ,"Within Specified Area"
        );
    }

    @Test
    public void testGetNumberOfDaysOfOperationalServiceInInterval() {
        LocalDate testIntervalBegin = LocalDate.of(2010, 12, 1);
        LocalDate testIntervalEnd = LocalDate.of(2011, 1, 31);
        // Fully contained within test period
        Deployment sixDays = makeDeployment(2010, 12, 4, 2010, 12, 9);
        // Outside the start period
        Deployment threeDays = makeDeployment(2010, 11, 15, 2010, 12, 3);
        // Outside the end period
        Deployment fiveDays = makeDeployment(2011, 1, 27, 2011, 6, 5);
        // Overlapping
        Deployment thirteenDaysPartialA = makeDeployment(2010, 12, 30, 2011, 1, 4);
        Deployment thirteenDaysPartialB = makeDeployment(2011, 1, 2, 2011, 1, 11);
        // Not operational
        Deployment notOperational = makeDeployment(2011, 1, 14, 2011, 1, 24);

        long result = ProcessingRuleFunctions.getNumberOfDaysOfOperationalServiceInInterval(testIntervalBegin
                , testIntervalEnd
                , ImmutableList.of(sixDays, threeDays, fiveDays, thirteenDaysPartialA, thirteenDaysPartialB, notOperational)
                , deployment -> {return !deployment.equals(notOperational);}
                , new SopSupportCaseTrace("unit test")
        );

        // 6 + 3 + 5 + 13 == 27
        Assert.assertTrue(result == 27);
    }

    private Service makeService(int beginYear, int beginMonth, int beginDay) {
        return new ServiceImpl(
            ServiceBranch.ARMY,
            EmploymentType.CFTS,
            Rank.Officer,
            LocalDate.of(beginYear, beginMonth, beginDay),
            Optional.empty(),
            null
        );
    }

    private Service makeService(int beginYear, int beginMonth, int beginDay, int endYear, int endMonth, int endDay) {
        return new ServiceImpl(
                ServiceBranch.ARMY,
                EmploymentType.CFTS,
                Rank.Officer,
                LocalDate.of(beginYear, beginMonth, beginDay),
                Optional.of(LocalDate.of(endYear, endMonth, endDay)),
                null
        );
    }

    @Test
    public void testGetDaysOfContinuousFullTimeServiceToDate() {
        ServiceHistory testData = new ServiceHistoryImpl(
            null,
            ImmutableSet.copyOf(Arrays.asList(
                    makeService(2011, 5, 10, 2011, 10, 10)
                    ,makeService(2011, 10, 10, 2012, 5, 10) // 367 (including the service before this one - 365 days + 1 leap day + 1 for inclusive)
                    ,makeService(2013, 1, 4, 2013, 1, 14) // 11
                    ,makeService(2014, 5, 1)
            ))
        );

        // too early for any service
        long days = ProcessingRuleFunctions.getDaysOfContinuousFullTimeServiceToDate(testData, LocalDate.of(2010, 1, 1));
        Assert.assertTrue(days == 0);

        // midway through the first pair of services
        days = ProcessingRuleFunctions.getDaysOfContinuousFullTimeServiceToDate(testData, LocalDate.of(2011, 6, 9));
        Assert.assertTrue(days == 31);

        // first pair of services
        days = ProcessingRuleFunctions.getDaysOfContinuousFullTimeServiceToDate(testData, LocalDate.of(2012, 11, 9));
        Assert.assertTrue(days == 367);

        // second pair of services
        days = ProcessingRuleFunctions.getDaysOfContinuousFullTimeServiceToDate(testData, LocalDate.of(2013, 6, 9));
        Assert.assertTrue(days == 378);

        // all of them
        days = ProcessingRuleFunctions.getDaysOfContinuousFullTimeServiceToDate(testData, LocalDate.of(2014, 5, 15));
        Assert.assertTrue(days == 393);

    }

    private Condition makeCondition(int beginYear, int beginMonth, int beginDay) {
        return new OnsetCondition(
            null,
            LocalDate.of(beginYear, beginMonth, beginDay),
            LocalDate.of(beginYear, beginMonth, beginDay),
            null
        );
    }
    @Test
    public void testConditionStartedWithinXYearsOfLastDayOfMRCAService() {
        ServiceHistory testData = new ServiceHistoryImpl(
                null,
                ImmutableSet.copyOf(Arrays.asList(
                        makeService(2011, 5, 10, 2011, 10, 10)
                        ,makeService(2011, 10, 10, 2012, 5, 10)
                        ,makeService(2013, 1, 4, 2013, 1, 14)
                ))
        );
        // Last day of MRCA service should be 2013-1-14
        boolean result;

        Condition ok = makeCondition(2017, 11, 24);
        result = ProcessingRuleFunctions.conditionStartedWithinXYearsOfLastDayOfMRCAService(ok, testData, 10, new SopSupportCaseTrace("unit test"));
        Assert.assertTrue(result);

        Condition notOk = makeCondition(2017, 11, 24);
        result = ProcessingRuleFunctions.conditionStartedWithinXYearsOfLastDayOfMRCAService(notOk, testData, 2, new SopSupportCaseTrace("unit test"));
        Assert.assertFalse(result);

        Condition edgeOk = makeCondition(2023, 1, 14);
        result = ProcessingRuleFunctions.conditionStartedWithinXYearsOfLastDayOfMRCAService(edgeOk, testData, 10, new SopSupportCaseTrace("unit test"));
        Assert.assertTrue(result);

        Condition edgeNotOk = makeCondition(2023, 1, 15);
        result = ProcessingRuleFunctions.conditionStartedWithinXYearsOfLastDayOfMRCAService(edgeNotOk, testData, 10, new SopSupportCaseTrace("unit test"));
        Assert.assertFalse(result);
    }
}
