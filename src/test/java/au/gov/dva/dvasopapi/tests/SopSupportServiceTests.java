package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.ConditionMock;
import au.gov.dva.dvasopapi.tests.mocks.LumbarSpondylosisConditionMock;
import au.gov.dva.dvasopapi.tests.mocks.ExtensiveServiceHistoryMock;
import au.gov.dva.dvasopapi.tests.mocks.processingRules.SimpleServiceHistory;
import au.gov.dva.sopapi.dtos.*;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.*;
import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopsupport.processingrules.rules.LumbarSpondylosisRule;
import au.gov.dva.sopapi.sopsupport.ruleconfiguration.CsvRuleConfigurationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static au.gov.dva.dvasopapi.tests.TestUtils.actOdtOf;
import static au.gov.dva.dvasopapi.tests.TestUtils.odtOf;

public class SopSupportServiceTests {

    Predicate<Deployment> isOperational = s -> !s.getOperationName().contains("Peace is Our Profession");


    private static SopSupportRequestDto buildOnsetTestDto() {
        return new SopSupportRequestDto(
                new ConditionDto(
                        "lumbar spondylosis",
                        IncidentType.Onset,
                        "ICD-10-AM",
                        "M47.16",
                        new OnsetDateRangeDto(
                                odtOf(2010, 1, 1),
                                odtOf(2010, 1, 1))
                        , null),

                new ServiceHistoryDto(
                        new ServiceSummaryInfoDto(odtOf(2004, 7, 1)),
                        ImmutableList.of(
                                new ServiceDto(
                                        ServiceBranch.ARMY,
                                        EmploymentType.CTFS,
                                        odtOf(2004, 7, 1),
                                        odtOf(2016, 1, 1),
                                        Rank.OtherRank,
                                        ImmutableList.of(
                                                new OperationalServiceDto(
                                                        odtOf(2006, 7, 1),
                                                        "Operation WARDEN",
                                                        "Within Specified Area",
                                                        odtOf(2006, 7, 10),
                                                        odtOf(2007, 7, 1)),
                                                new OperationalServiceDto(
                                                        odtOf(2009, 7, 1),
                                                        "Operation RIVERBANK",
                                                        "Within Specified Area",
                                                        odtOf(2009, 7, 10),
                                                        odtOf(2010, 7, 1)
                                                )
                                        )
                                )
                        )
                ));
    }

    private static SopSupportRequestDto buildTestDto() {

        return new SopSupportRequestDto(
                new ConditionDto(
                        "lumbar spondylosis",
                        IncidentType.Aggravation,
                        "ICD-10-AM",
                        "M47.16",
                        new OnsetDateRangeDto(
                                odtOf(2010, 1, 1),
                                odtOf(2010, 1, 1))
                        , new AggravationDateRangeDto(
                        odtOf(2005,1,1),
                        odtOf(2005,1,15)
                )),

                new ServiceHistoryDto(
                        new ServiceSummaryInfoDto(odtOf(2004, 7, 1)),
                        ImmutableList.of(
                                new ServiceDto(
                                        ServiceBranch.RAN,
                                        EmploymentType.CTFS,
                                        odtOf(2004, 7, 1),
                                        odtOf(2016, 1, 1),
                                        Rank.OtherRank,
                                        ImmutableList.of(
                                                new OperationalServiceDto(
                                                        odtOf(2006, 7, 1),
                                                        "Operation WARDEN",
                                                        "Within Specified Area",
                                                        odtOf(2006, 7, 10),
                                                        odtOf(2007, 7, 1)),
                                                new OperationalServiceDto(
                                                        odtOf(2009, 7, 1),
                                                        "Operation RIVERBANK",
                                                        "Within Specified Area",
                                                        odtOf(2009, 7, 10),
                                                        odtOf(2010, 7, 1)
                                                )
                                        )
                                )
                        )
                ));
    }

    @Test
    public void testDtoSerialization() {
        SopSupportRequestDto testData = SopSupportServiceTests.buildTestDto();
        String jsonString = testData.toJsonString();
        System.out.print(jsonString);
        Assert.assertTrue(!jsonString.isEmpty());
    }


    @Test
    public void testDtoDeserialization() throws IOException {
        URL jsonUrl = Resources.getResource("sopSupportDto.json");
        String jsonString = Resources.toString(jsonUrl, Charsets.UTF_8);

        SopSupportRequestDto result = SopSupportRequestDto.fromJsonString(jsonString);
        Assert.assertTrue(result != null);

    }

    @Test
    public void testUtcDateParse()
    {
        String testString = "2010-01-01T00:00:00Z";
        OffsetDateTime result = OffsetDateTime.parse(testString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        Assert.assertTrue(result != null);
    }

    @Test
    public void generateJsonSchema() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING,true);
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);

        JsonSchema schema = schemaGen.generateSchema(SopSupportRequestDto.class);

        String schemaString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);

        System.out.print(schemaString);
        Assert.assertTrue(schema != null);
    }

    @Test
    public void getTimeZoneIds()
    {
        Set<String> ids =  ZoneId.getAvailableZoneIds();
        Assert.assertTrue(ids.contains("Australia/ACT"));
        System.out.print(String.join("\n",ids.stream().filter(i -> i.startsWith("Australia")).sorted().collect(Collectors.toList())));

    }

    @Test
    public void testDeploymentGenerator() throws IOException {
        ImmutableList<Deployment> results = TestUtils.getTestDeployments();
        for (Deployment d : results)
        {
            System.out.printf("Start: %s; End: %s Name: %s\n", d.getStartDate(), d.getEndDate().get(), d.getOperationName());

        }
        Assert.assertTrue(!results.isEmpty());
    }

    @Test
    public void testLsSopIdentification() throws IOException {
        RuleConfigurationRepository ruleConfigurationRepository = getRuleConfig();
        ProcessingRule underTest = new LumbarSpondylosisRule(ruleConfigurationRepository);
        Condition condition = new LumbarSpondylosisConditionMock();
        Optional<SoP> applicableSop = underTest.getApplicableSop(condition,new ExtensiveServiceHistoryMock(),isOperational);
        Assert.assertTrue(applicableSop.get().getStandardOfProof() == StandardOfProof.ReasonableHypothesis);


    }

    @Test
    public void testFailOperationalServiceReq() throws IOException {
        ProcessingRule underTest = new LumbarSpondylosisRule(getRuleConfig());
        Condition onsetBeforeAnyOpService = new ConditionMock(new LumbarSpondylosisConditionMock().getSopPair(),actOdtOf(2004,8,1),actOdtOf(2004,8,1),null);
        ServiceHistory serviceHistory = SimpleServiceHistory.get();
        Optional<SoP> applicableSop = underTest.getApplicableSop(onsetBeforeAnyOpService,serviceHistory,isOperational);
        Assert.assertTrue(applicableSop.get().getStandardOfProof() == StandardOfProof.BalanceOfProbabilities);

    }

    @Test
    public void testPassOperationalServiceReq() throws IOException {
        ProcessingRule underTest = new LumbarSpondylosisRule(getRuleConfig());
        Condition onsetBeforeAnyOpService = new ConditionMock(new LumbarSpondylosisConditionMock().getSopPair(),actOdtOf(2004,10,1),actOdtOf(2004,10,1),null);
        ServiceHistory serviceHistory = SimpleServiceHistory.get();
        Optional<SoP> applicableSop = underTest.getApplicableSop(onsetBeforeAnyOpService,serviceHistory,isOperational);
        Assert.assertTrue(applicableSop.get().getStandardOfProof() == StandardOfProof.ReasonableHypothesis);

    }

    private RuleConfigurationRepository getRuleConfig() throws IOException {
        byte[] rhCsv = Resources.toByteArray(Resources.getResource("rulesConfiguration/RH.csv"));
        byte[] boPCsv = Resources.toByteArray(Resources.getResource("rulesConfiguration/BoP.csv"));
        RuleConfigurationRepository repo = new CsvRuleConfigurationRepository(rhCsv, boPCsv);
        return repo;
    }


}
