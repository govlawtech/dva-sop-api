package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.ProcessingRule;
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



    @Test
    public void SimpleTestWithOnlyOneDeployment() {
        LocalDate testIntervalBeginning = LocalDate.of(2010,1,1);
        LocalDate testIntevalEnd = LocalDate.of(2010,12,31);
        Deployment singleDayDeployment = makeDeployment(2010,1,1,2010,1,2);
        long result = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(testIntervalBeginning,testIntevalEnd,ImmutableList.of(singleDayDeployment));
        // Includes end date
        Assert.assertTrue(result == 2);
    }

    @Test
    public void TestWithOverappingDeployments() {
        LocalDate testIntervalBeginning = LocalDate.of(2010,1,1);
        LocalDate testIntevalEnd = LocalDate.of(2010,1,3);
        Deployment firstDeployment = makeDeployment(2010,1,1,2010, 1,2);
        Deployment secondDeployment = makeDeployment(2010,1,2,2010,1,3);

        long result = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(testIntervalBeginning,testIntevalEnd,ImmutableList.of(firstDeployment,secondDeployment));
        // Includes end date
        Assert.assertTrue(result == 3);
    }


    @Test
    public void LayoshiTest() {
        LocalDate serviceStartDate = LocalDate.of(2010,1,21);
        LocalDate serviceEndDate = LocalDate.now();

        Deployment a1 = makeDeployment(2012,11,30,2013,3,8);
        Deployment a2 = makeDeployment(2013,3,18,2013,4,3);
        Deployment r1 = makeDeployment(2015,12,4,2015,12,5);
        Deployment r2 = makeDeployment(2015,12,5,2016,1,5);
        Deployment r3 = makeDeployment(2016,1,5,2016,1,6);
        Deployment r4 = makeDeployment(2016,1,6,2016,1,14);
        Deployment r5 = makeDeployment(2016,1,14,2016,2,23);
        Deployment r6 = makeDeployment(2016,2,23,2016,2,26);

        // Expected:
        // ANODE1 including end date: 99
        // ANODE2 including end date: 17
        // RESOLUTE1 to RESOLUTE6 including end date: 85
        // total: 201

        long result= ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(serviceStartDate,serviceEndDate,ImmutableList.of(a1,a2,r1,r2,r3,r4,r5,r6));

        Assert.assertTrue(result == 201);

    }

    @Test
    public void TestServiceBranchInterval()
    {
        LocalDate testIntervalBeginning = LocalDate.of(2010,1,1);
        LocalDate testIntevalEnd = LocalDate.of(2010,12,31);
        Service testService = makeService(2010,1,1,2010,1,2);
        long days = ProcessingRuleFunctions.getNumberOfDaysOfServiceInInterval(testIntervalBeginning,testIntevalEnd,ImmutableList.of(testService));
        Assert.assertTrue(days == 2);
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


}
