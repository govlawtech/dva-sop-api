package au.gov.dva.dvasopapi.tests.localonly;

import au.gov.dva.dvasopapi.tests.TestUtils;
import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislation;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import au.gov.dva.sopapi.sopref.data.updates.types.NewInstrument;
import au.gov.dva.sopapi.sopref.data.updates.LegRegChangeDetector;
import au.gov.dva.sopapi.sopref.data.updates.SoPLoader;
import au.gov.dva.sopapi.sopref.parsing.factories.ServiceLocator;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class IntegrationTests {

    @Test
    @Category(IntegrationTests.class)
    public void getSoPTaskTest() throws ExecutionException, InterruptedException, JsonProcessingException {

        Repository localRepository = new AzureStorageRepository("UseDevelopmentStorage=true");
        SoPLoader underTest = new SoPLoader(localRepository, new FederalRegisterOfLegislation(),s -> ServiceLocator.findTextCleanser(s),s -> ServiceLocator.findSoPFactory(s));
        CompletableFuture<Optional<SoP>> result = underTest.createGetSopTask("F2014L00933");
        Assert.assertTrue(result.get().isPresent());
        SoP sop = result.get().get();

        System.out.println(TestUtils.prettyPrint(StoredSop.toJson(sop)));
    }


    @Test
    @Category(IntegrationTests.class)
    public void sopLoaderNewInstrument()
    {

        Repository localRepository = new AzureStorageRepository("UseDevelopmentStorage=true");
        // setup
        localRepository.deleteSoPIfExists("F2014L00933");
        NewInstrument newInstrument = new NewInstrument("F2014L00933", DateTimeUtils.localDateToMidnightACTDate(LocalDate.of(2017,1,1)));
        localRepository.addInstrumentChange(newInstrument);
        // end setup


        SoPLoader underTest = new SoPLoader(localRepository, new FederalRegisterOfLegislation(),s -> ServiceLocator.findTextCleanser(s),s -> ServiceLocator.findSoPFactory(s));
        underTest.applyAll(10);

    }

    @Test
    @Category(IntegrationTests.class)
    public void sopLoaderReplacedInstrument()
    {
        //   "F2008L03179" is repealed by "F2017L00016";

        String originalRegisterId = "F2008L03179";
        String repealingRegisterId = "F2017L00016";
        Repository localRepository = new AzureStorageRepository("UseDevelopmentStorage=true");

        // setup
        localRepository.deleteSoPIfExists(originalRegisterId);
        localRepository.deleteSoPIfExists(repealingRegisterId);

        RegisterClient registerClient = new FederalRegisterOfLegislation();

        SoPLoader underTest = new SoPLoader(localRepository, new FederalRegisterOfLegislation(),s -> ServiceLocator.findTextCleanser(s),s -> ServiceLocator.findSoPFactory(s));
        underTest.applyAll(10);

        LegRegChangeDetector legRegChangeDetector = new LegRegChangeDetector(registerClient);

    }
}
