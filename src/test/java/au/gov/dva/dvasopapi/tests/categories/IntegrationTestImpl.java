package au.gov.dva.dvasopapi.tests.categories;

import au.gov.dva.sopapi.AppSettings;
import au.gov.dva.sopapi.Environment;
import org.junit.Before;

public class IntegrationTestImpl implements IntegrationTest {
    @Before
    public void beforeMethod() {
       // org.junit.Assume.assumeTrue(AppSettings.isEnvironmentSet() && AppSettings.getEnvironment() == Environment.devtest);
    }
}
