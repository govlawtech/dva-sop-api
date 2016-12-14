package au.gov.dva.dvasopapi.tests.localonly;

import au.gov.dva.dvasopapi.tests.TestUtils;
import au.gov.dva.dvasopapi.tests.categories.IntegrationTest;
import au.gov.dva.dvasopapi.tests.mocks.MockLumbarSpondylosisSop;
import au.gov.dva.sopref.data.AzureStorageRepository;
import au.gov.dva.sopref.data.sops.StoredSop;
import au.gov.dva.interfaces.Repository;
import au.gov.dva.interfaces.model.SoP;
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

        Iterable<SoP> allSops = underTest.getAllSops();
        Assert.assertTrue(allSops.iterator().hasNext());
    }


    @Category(IntegrationTest.class)
    @Test
    public void missingSoPShouldBeEmptyOptional()
    {
        Repository underTest = new AzureStorageRepository("UseDevelopmentStorage=true");
        Optional<SoP> shouldBeEmpty = underTest.getSop("Mr Funny Pants");
        Assert.assertTrue(!shouldBeEmpty.isPresent());
    }

}


