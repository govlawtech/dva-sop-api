package au.gov.dva.dvasopapi.tests.localonly;

import au.gov.dva.dvasopapi.tests.TestUtils;
import au.gov.dva.dvasopapi.tests.categories.IntegrationTest;
import au.gov.dva.dvasopapi.tests.mocks.MockLumbarSpondylosisSopRH;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableSet;
import com.microsoft.azure.storage.StorageException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Optional;

public class AzureStorageTests {



    @Category(IntegrationTest.class)
    @Test
    public void saveAndRetrieveSop() throws JsonProcessingException {
        Repository underTest = new AzureStorageRepository("UseDevelopmentStorage=true");
        SoP mockSop = new MockLumbarSpondylosisSopRH();
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

    @Category(IntegrationTest.class)
    @Test
    public void getInstrumentChanges() throws StorageException {
        Repository underTest = new AzureStorageRepository("UseDevelopmentStorage=true");
        ImmutableSet<InstrumentChange> results = underTest.getInstrumentChanges();
        results.forEach(instrumentChange -> System.out.println(instrumentChange));
        Assert.assertTrue(results.size() > 0);
    }

}


