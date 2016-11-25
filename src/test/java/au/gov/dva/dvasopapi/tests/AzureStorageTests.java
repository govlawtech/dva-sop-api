package au.gov.dva.dvasopapi.tests;

import au.gov.dva.AppSettings;
import au.gov.dva.dvasopapi.tests.categories.IntegrationTest;
import au.gov.dva.dvasopapi.tests.mocks.MockLumbarSpondylosisSop;
import au.gov.dva.sopref.data.AzureStorageRepository;
import au.gov.dva.sopref.data.SoPs.StoredSop;
import au.gov.dva.sopref.interfaces.Repository;
import au.gov.dva.sopref.interfaces.model.SoP;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Optional;

public class AzureStorageTests {

    @Category(IntegrationTest.class)
    @Test
    public void saveAndRetrieveSop() throws JsonProcessingException {
        Repository underTest = new AzureStorageRepository("UseDevelopmentStorage=true");
        SoP mockSop = new MockLumbarSpondylosisSop();
        underTest.saveSop(mockSop);
        Optional<SoP> retrieved = underTest.getSop(mockSop.getRegisterId());
        Assert.assertTrue(retrieved.isPresent());
        System.out.print(TestUtils.prettyPrint(StoredSop.toJson(retrieved.get())));
    }

}
