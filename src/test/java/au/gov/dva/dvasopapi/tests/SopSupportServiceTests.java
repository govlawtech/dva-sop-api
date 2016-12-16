package au.gov.dva.dvasopapi.tests;

import java.time.OffsetDateTime;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.sopsupport.RequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class SopSupportServiceTests {

    private static OffsetDateTime odtOf(int year, int month, int day) {
        return OffsetDateTime.of(year, month, day, 0, 0, 0, 0, ZoneOffset.UTC);
    }

    private static RequestDto buildTestDto() {
        return new RequestDto(
                new ConditionDto(
                        "lumbar spondylosis",
                        IncidentType.Aggravation,
                        "ICD-10-AM",
                        "M39.89",
                        new OnsetDateRangeDto(
                                odtOf(2010, 1, 1),
                                null,
                                null)
                        , new AggravationDateRangeDto(
                              null,
                        odtOf(2005,1,1),
                        odtOf(2005,1,15)
                )),

                new ServiceHistoryDto(
                        new ServiceSummaryInfoDto(odtOf(2004, 7, 1)),
                        ImmutableList.of(
                                new ServiceDto(
                                        "Royal Australian Air Force",
                                        "Regular/Permanent Force",
                                        odtOf(2004, 7, 1),
                                        odtOf(2016, 1, 1),
                                        au.gov.dva.sopapi.dtos.Rank.OtherRank,
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
        RequestDto testData = buildTestDto();
        String jsonString = testData.toJsonString();
        System.out.print(jsonString);
        Assert.assertTrue(!jsonString.isEmpty());
    }

    @Ignore
    @Test
    public void testDtoDeserialization() throws IOException {
        URL jsonUrl = Resources.getResource("sopSupportDto.json");
        String jsonString = Resources.toString(jsonUrl, Charsets.UTF_8);

        RequestDto result = RequestDto.fromJsonString(jsonString);
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

        JsonSchema schema = schemaGen.generateSchema(RequestDto.class);

        String schemaString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);

        System.out.print(schemaString);
        Assert.assertTrue(schema != null);
    }
}
