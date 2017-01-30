package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.categories.IntegrationTest;
import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.interfaces.InstrumentChangeFactory;
import au.gov.dva.sopapi.interfaces.LegislationRegisterEmailClient;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import au.gov.dva.sopapi.interfaces.model.InstrumentChangeBase;
import au.gov.dva.sopapi.interfaces.model.LegislationRegisterEmailUpdate;
import au.gov.dva.sopapi.sopref.data.FederalRegisterOfLegislationClient;
import au.gov.dva.sopapi.sopref.data.JsonUtils;
import au.gov.dva.sopapi.sopref.data.updates.LegRegChangeDetector;
import au.gov.dva.sopapi.sopref.data.updates.changefactories.EmailSubscriptionInstrumentChangeFactory;
import au.gov.dva.sopapi.sopref.data.updates.types.NewInstrument;
import au.gov.dva.sopapi.sopref.data.updates.types.Replacement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.copyOf;

public class AutoUpdateTests {

    @Test
    public void serializeNewInstrument() throws JsonProcessingException {
        InstrumentChange test = new NewInstrument("F2014L83848", DateTimeUtils.localDateToMidnightACTDate(LocalDate.of(2015,1,1)));
        JsonNode node = test.toJson();
        System.out.print(TestUtils.prettyPrint(node));
        Assert.assertTrue(node != null);
    }

    @Test
    public void deserializeUpdatesList() throws IOException {
        String updatesString = Resources.toString(Resources.getResource("updates/updates.json"), Charsets.UTF_8);
        JsonNode jsonNode = (new ObjectMapper()).readTree(updatesString);
        ImmutableList<JsonNode> jsonObjects = JsonUtils.getChildrenOfArrayNode(jsonNode);
        List<InstrumentChange> results = jsonObjects.stream().map(n -> InstrumentChangeBase.fromJson(n)).collect(Collectors.toList());
        results.forEach(r -> System.out.println(r));
        Assert.assertTrue(results.size() == 5);
    }

    @Ignore
    @Test
    public void createInitialNewInstrumentsJson() throws IOException {
        String[] rhList = Resources.toString(Resources.getResource("rhSopRegisterIds.txt"),Charsets.UTF_8).split("[\r\n]+");
        String[] bopList = Resources.toString(Resources.getResource("bopSopRegisterIds.txt"),Charsets.UTF_8).split("[\r\n]+");
        Assert.assertTrue(rhList.length == 52);
        Assert.assertTrue(bopList.length == 52);
        ImmutableList<String> rh = copyOf(rhList);
        ImmutableList<String> bop = copyOf(bopList);
        ImmutableList<String> both =  new ImmutableList.Builder<String>().addAll(rh).addAll(bop).build();
        OffsetDateTime creationDate = DateTimeUtils.localDateToMidnightACTDate(LocalDate.of(2017,1,6));
        Stream<JsonNode> instrumentChangeStream = both.stream()
                .map(id -> new NewInstrument(id,creationDate))
                .map(ni -> ni.toJson());

        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        instrumentChangeStream.forEach(jsonNode -> arrayNode.add(jsonNode));
        System.out.println(TestUtils.prettyPrint(arrayNode));

    }

    @Test
    public void testRedirectUrlGet() throws MalformedURLException {
        URL redirectTarget = URI.create("http://www.legislation.gov.au/Details/F2014C383817").toURL();

        String result = FederalRegisterOfLegislationClient.extractTargetRegisterIdFromRedirectUrl(redirectTarget);
        Assert.assertTrue(result.contentEquals("F2014C383817"));
    }

    @Test
    @Category(IntegrationTest.class)
    public void testBulkRedirectTargetGet() {
        ImmutableSet<String> testSourceIds = ImmutableSet.of(
                "F2014L01390", // Statement of Principles concerning anxiety disorder No. 103 of 2014,  already amended with compilation
                "F2014L01389", // Statement of Principles concerning anxiety disorder No. 102 of 2014,  already amended with compilation
                "F2010L00557"  // Statement of Principles concerning osteoarthritis No. 13 of 2010, already amended with compilation
        );

        LegRegChangeDetector underTest = new LegRegChangeDetector(new FederalRegisterOfLegislationClient());
        ImmutableSet<InstrumentChange> newCompilations = underTest.detectNewCompilations(testSourceIds);

        for (InstrumentChange s : newCompilations)
        {
            System.out.println(s);
        }

        Assert.assertTrue(newCompilations.size() == 3);
    }

    @Test
    @Category(IntegrationTest.class)
    public void testGetRepealingRegisterId() {
        ImmutableSet<String> testSourceIds = ImmutableSet.of(
                "F2008L03179"
        );
        String expectedIdOfRepealingInstrument = "F2017L00016";

        LegRegChangeDetector underTest = new LegRegChangeDetector(new FederalRegisterOfLegislationClient());
        ImmutableSet<InstrumentChange> results  = underTest.detectReplacements(testSourceIds);
        results.stream().forEach(r -> System.out.println(r));
        Replacement result = (Replacement)results.asList().get(0);
        Assert.assertTrue(result.getSourceInstrumentId().contentEquals(testSourceIds.asList().get(0)));
        Assert.assertTrue(result.getTargetInstrumentId().contentEquals(expectedIdOfRepealingInstrument));
    }

    @Test
    @Category(IntegrationTest.class)
    // This test is obviously going to start failing if the instrument is actually repealed.
    public void testGetRepealingIdWhenNoneExists()
    {
        ImmutableSet<String> testSourceIds = ImmutableSet.of(
                "F2014L00930"
        );
        LegRegChangeDetector underTest = new LegRegChangeDetector(new FederalRegisterOfLegislationClient());
        ImmutableSet<InstrumentChange> results = underTest.detectReplacements(testSourceIds);
        Assert.assertTrue(results.isEmpty());
    }


    private class TestEmailUpdate implements LegislationRegisterEmailUpdate
    {
        private final String title;
        private final String url;
        private final String updateDesc;

        public TestEmailUpdate(String title, String url, String updateDesc)
        {
            this.title = title;
            this.url = url;
            this.updateDesc = updateDesc;
        }

        @Override
        public String getInstrumentTitle() {
            return title;
        }


        @Override
        public String getUpdateDescription() {
            return updateDesc;
        }

        @Override
        public URL getRegisterLink() {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public OffsetDateTime getDateReceived() {
            return OffsetDateTime.now();
        }
    }


    private class MockEmailClient implements LegislationRegisterEmailClient {
        TestEmailUpdate amendment1 = new TestEmailUpdate("Amendment Statement of Principles concerning malignant neoplasm of the oesophagus (Reasonable Hypothesis) (No. 21 of 2017)",
                "https://www.legislation.gov.au/Details/F2017L00018",
                "Item was published on 4/01/2017");

        TestEmailUpdate publish1 = new TestEmailUpdate("Statement of Principles concerning ascariasis (Reasonable Hypothesis) (No. 9 of 2017)",
                "https://www.legislation.gov.au/Details/F2017L00015",
                "Item was published on 4/01/2017");

        @Override
        public CompletableFuture<ImmutableSet<LegislationRegisterEmailUpdate>> getUpdatesBetween(OffsetDateTime startDateExclusive, OffsetDateTime endDateExclusive) {
            return CompletableFuture.completedFuture(ImmutableSet.of(
                    amendment1,
                    publish1
            ));
        }
    }

    @Test
    public void testEmailUpdateFactory()
    {
        LegislationRegisterEmailClient mockClient = new MockEmailClient();
        Supplier<OffsetDateTime> fromDateSupplier = () -> OffsetDateTime.of(2017,1,1,0,0,0,0, ZoneOffset.UTC);
        InstrumentChangeFactory underTest = new EmailSubscriptionInstrumentChangeFactory(mockClient,fromDateSupplier);
        ImmutableSet<InstrumentChange> results = underTest.getChanges();
        Assert.assertTrue(results.size() == 1 && results.stream().findFirst().get().getTargetInstrumentId().contentEquals("F2017L00015"));
    }

}
