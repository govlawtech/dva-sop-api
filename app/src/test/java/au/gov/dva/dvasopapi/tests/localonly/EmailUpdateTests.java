package au.gov.dva.dvasopapi.tests.localonly;

import au.gov.dva.dvasopapi.tests.categories.IntegrationTest;
import au.gov.dva.sopapi.interfaces.model.LegislationRegisterEmailUpdate;
import au.gov.dva.sopapi.sopref.data.updates.LegislationRegisterEmailUpdates;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EmailUpdateTests {

    @Test
    @Category(IntegrationTest.class)
    public void testEmailClient() throws ExecutionException, InterruptedException {
        // The devtest email box already has two emails sent to it in the time bracket below
        // To run this test, you need to set the secrets  "LRS_USERID" and "LRS_PASSWORD" as environment variables or jvm args.

        String emailSender = "nick.miller@govlawtech.com.au";
        OffsetDateTime dateBeforeTestEmailSent = OffsetDateTime.of(2017,1,23,0,0,0,0, ZoneOffset.ofHours(10));
        OffsetDateTime dateAfterTestEmailSent = OffsetDateTime.of(2017,1,24,23,0,0,0, ZoneOffset.ofHours(10));
        CompletableFuture<ImmutableSet<LegislationRegisterEmailUpdate>> resultFromEmailsForwardedByNm =
            LegislationRegisterEmailUpdates.getEmailsReceivedBetween(dateBeforeTestEmailSent, dateAfterTestEmailSent, emailSender);

        ImmutableSet<LegislationRegisterEmailUpdate> results = resultFromEmailsForwardedByNm.get();
        Assert.assertTrue(results.size() == 25);
        Assert.assertTrue(results.stream().filter(r -> r.getUpdateDescription().contains("Compilation")).count() == 1);

        results.stream().forEach(r -> System.out.println(r));


    }

}
