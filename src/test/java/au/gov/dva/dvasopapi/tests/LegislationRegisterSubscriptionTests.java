package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.categories.IntegrationTest;
import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.interfaces.model.LegislationRegisterEmailUpdate;
import au.gov.dva.sopapi.sopref.data.updates.LegislationRegisterEmailUpdates;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

public class LegislationRegisterSubscriptionTests {
    @Test
    @Ignore
    @Category(IntegrationTest.class)
    public void retrieve() throws ExecutionException, InterruptedException {
        // todo: you'll need to send some emails to the devtest account to test
        ImmutableSet<LegislationRegisterEmailUpdate> results = LegislationRegisterEmailUpdates
                .getLatestAfter(DateTimeUtils.localDateToMidnightACTDate(LocalDate.of(2017,1,11))).get();

        Assert.assertTrue(!results.isEmpty());
    }
}
