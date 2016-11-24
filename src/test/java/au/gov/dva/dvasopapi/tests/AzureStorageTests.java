package au.gov.dva.dvasopapi.tests;

import au.gov.dva.AppSettings;
import au.gov.dva.dvasopapi.tests.categories.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class AzureStorageTests {

    @Category(IntegrationTest.class)
    @Test
    public void saveAndRetrieveSop() {
        String accountName = AppSettings.DevTest.AzureStorage.accountName;
    }

}
