package au.gov.dva.dvasopapi.tests.localonly;

import au.gov.dva.dvasopapi.tests.TestUtils;
import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.interfaces.InstrumentChangeFactory;
import au.gov.dva.sopapi.interfaces.RegisterClient;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.SoPLoader;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import au.gov.dva.sopapi.sopref.data.updates.AutoUpdate;
import au.gov.dva.sopapi.sopref.data.updates.LegislationRegisterEmailClientImpl;
import au.gov.dva.sopapi.sopref.data.updates.SoPLoaderImpl;
import au.gov.dva.sopapi.sopref.data.updates.changefactories.EmailSubscriptionInstrumentChangeFactory;
import au.gov.dva.sopapi.sopref.data.updates.changefactories.LegislationRegisterSiteChangeFactory;
import au.gov.dva.sopapi.sopref.data.updates.types.NewInstrument;
import au.gov.dva.sopapi.sopref.parsing.factories.ServiceLocator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableSet;
import com.microsoft.azure.storage.StorageException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;


// You need to use the JUnit runner to run these - they are excluded from the Gradle runner
// as they require local configuration.
public class IntegrationTests {

    @Test
    @Category(IntegrationTests.class)
    public void getSoPTaskTest() throws ExecutionException, InterruptedException, JsonProcessingException {

        Repository localRepository = new AzureStorageRepository("UseDevelopmentStorage=true");
        SoPLoaderImpl underTest = new SoPLoaderImpl(localRepository, new FederalRegisterOfLegislationClient(), s -> ServiceLocator.findTextCleanser(s), s -> ServiceLocator.findSoPFactory(s));
        CompletableFuture<Optional<SoP>> result = underTest.createGetSopTask("F2014L00933");
        Assert.assertTrue(result.get().isPresent());
        SoP sop = result.get().get();

        System.out.println(TestUtils.prettyPrint(StoredSop.toJson(sop)));
    }


    @Test
    @Category(IntegrationTests.class)
    public void sopLoaderNewInstrument() throws StorageException {

        AzureStorageRepository localRepository = new AzureStorageRepository("UseDevelopmentStorage=true");
        // setup
        localRepository.purge();
        String registerId = "F2014L00933";


        NewInstrument newInstrument = new NewInstrument("F2014L00933", DateTimeUtils.localDateToMidnightACTDate(LocalDate.of(2017,1,1)));
        localRepository.addInstrumentChanges(ImmutableSet.of(newInstrument));
        // end setup


        SoPLoader underTest = new SoPLoaderImpl(localRepository, new FederalRegisterOfLegislationClient(), s -> ServiceLocator.findTextCleanser(s), s -> ServiceLocator.findSoPFactory(s));
        underTest.applyAll(10);
        Assert.assertTrue(localRepository.getSop(registerId).isPresent());


    }

    @Test
    @Category(IntegrationTests.class)
    public void sopLoaderReplacedInstrument() throws StorageException {
        //   "F2008L03179" is repealed by "F2017L00016";

        String originalRegisterId = "F2008L03179";
        String repealingRegisterId = "F2017L00016";
        AzureStorageRepository localRepository = new AzureStorageRepository("UseDevelopmentStorage=true");

        // setup
        localRepository.purge();

        SoPLoader underTest = new SoPLoaderImpl(localRepository, new FederalRegisterOfLegislationClient(), s -> ServiceLocator.findTextCleanser(s), s -> ServiceLocator.findSoPFactory(s));
        underTest.applyAll(10);


    }

    // this needs the email user id and pass set
    @Test
    @Category(IntegrationTests.class)
    public void patchRepository() throws StorageException {
        AzureStorageRepository localRepository = new AzureStorageRepository("UseDevelopmentStorage=true");
        localRepository.purge();

        // There is test data in devtest email box after this date
        OffsetDateTime lastUpdated =  OffsetDateTime.of(2017,1,23,0,0,0,0, ZoneOffset.ofHours(10));
        localRepository.setLastUpdated(lastUpdated);
        Supplier<OffsetDateTime> getLastUpdatedDate = () -> localRepository.getLastUpdated().get();

        InstrumentChangeFactory emailChangeFactory = new EmailSubscriptionInstrumentChangeFactory(new LegislationRegisterEmailClientImpl("nick.miller@govlawtech.com.au"),getLastUpdatedDate);

        RegisterClient registerClient = new FederalRegisterOfLegislationClient();
        Supplier<ImmutableSet<String>> getExistingIds = () -> localRepository.getAllSops().stream()
                .map(soP -> soP.getRegisterId())
                .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableSet::copyOf));
        InstrumentChangeFactory updateChangeFactory = new LegislationRegisterSiteChangeFactory(registerClient,getExistingIds);

        // only changes should be one published compilation instrument which came from email:
        //Statement of Principles concerning malignant neoplasm of the oesophagus (Reasonable Hypothesis) (No. 120 of 2015)
        // F2017C00077
        AutoUpdate.updateChangeList(localRepository,emailChangeFactory,updateChangeFactory);

        ImmutableSet<InstrumentChange> updatesInRepo = localRepository.getInstrumentChanges();
        Assert.assertTrue(updatesInRepo.size() == 1);
        Assert.assertTrue(updatesInRepo.stream().findFirst().get().getSourceInstrumentId().contentEquals("F2017C00077"));



    }


}
