package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.OnsetCondition;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import au.gov.dva.sopapi.sopsupport.processingrules.DeploymentImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import au.gov.dva.sopapi.sopsupport.processingrules.ServiceImpl;
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

        long result = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(testIntervalBegin
                , testIntervalEnd
                , ImmutableList.of(sixDays, threeDays, fiveDays, thirteenDaysPartialA, thirteenDaysPartialB)
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
    public void testGetDaysOfServiceInInterval() {
        ImmutableList<Service> testData =
            ImmutableList.copyOf(Arrays.asList(
                    makeService(2011, 5, 10, 2011, 10, 10)
                    ,makeService(2011, 10, 10, 2012, 5, 10) // 367 (including the service before this one - 365 days + 1 leap day + 1 for inclusive)
                    ,makeService(2013, 1, 4, 2013, 1, 14) // 11
                    ,makeService(2014, 5, 1)
            ));


        LocalDate early = LocalDate.of(1900, 1, 1);
        // too early for any service
        long days = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(early, LocalDate.of(2010, 1, 1), testData);
        Assert.assertTrue(days == 0);

        // midway through the first pair of services
        days = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(early, LocalDate.of(2011, 6, 9), testData);
        Assert.assertTrue(days == 31);

        // first pair of services
        days = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(early, LocalDate.of(2012, 11, 9), testData);
        Assert.assertTrue(days == 367);

        // second pair of services
        days = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(early, LocalDate.of(2013, 6, 9), testData);
        Assert.assertTrue(days == 378);

        // all of them
        days = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(early, LocalDate.of(2014, 5, 15), testData);
        Assert.assertTrue(days == 393);

        // all of them - clamped by a start date
        days = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(
                LocalDate.of(2012, 5, 7)
                , LocalDate.of(2014, 5, 15)
                , testData);
        Assert.assertTrue(days == 30); // four days from the big service + 26 from the later services
    }

    private Condition makeCondition(int beginYear, int beginMonth, int beginDay) {
        return new OnsetCondition(
            null,
            LocalDate.of(beginYear, beginMonth, beginDay),
            LocalDate.of(beginYear, beginMonth, beginDay),
            null
        );
    }

    private void failGetStartOfOnsetWindow(String periodSpecifier, LocalDate onsetDate) {
        try {
            LocalDate result = ProcessingRuleFunctions.getStartOfOnsetWindow(periodSpecifier, onsetDate);
            Assert.fail("should have thrown an exception");
        }
        catch (RuntimeException ex) {

        }
    }

    private void successGetStartOfOnsetWindow(String periodSpecifier, LocalDate onsetDate, LocalDate expectedResult) {
        LocalDate result = ProcessingRuleFunctions.getStartOfOnsetWindow(periodSpecifier, onsetDate);
        Assert.assertEquals(expectedResult, result);
    }

    @Test
    public void testGetStartOfOnsetWindow() {
        LocalDate onsetDate = LocalDate.of(2011, 5, 1);

        // test null onset date
        failGetStartOfOnsetWindow("1d", null);

        // test null period specifier
        failGetStartOfOnsetWindow(null, onsetDate);

        // test bad period specifiers
        failGetStartOfOnsetWindow("d324d", onsetDate);
        failGetStartOfOnsetWindow("324w", onsetDate);

        // test years
        successGetStartOfOnsetWindow("1y", onsetDate, LocalDate.of(2010, 5, 1));
        successGetStartOfOnsetWindow("5y", onsetDate, LocalDate.of(2006, 5, 1));
        successGetStartOfOnsetWindow("15y", onsetDate, LocalDate.of(1996, 5, 1));

        // test days
        successGetStartOfOnsetWindow("0d", onsetDate, LocalDate.of(2011, 5, 1));
        successGetStartOfOnsetWindow("1d", onsetDate, LocalDate.of(2011, 4, 30));
        successGetStartOfOnsetWindow("5d", onsetDate, LocalDate.of(2011, 4, 26));
        successGetStartOfOnsetWindow("15d", onsetDate, LocalDate.of(2011, 4, 16));
        successGetStartOfOnsetWindow("131d", onsetDate, LocalDate.of(2010, 12, 21));
    }
}
