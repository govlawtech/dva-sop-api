package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
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
}
