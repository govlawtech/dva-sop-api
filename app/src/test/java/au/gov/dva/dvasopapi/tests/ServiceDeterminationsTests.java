package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopapi.interfaces.model.Operation;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.sopref.data.Conversions;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.StoredOperation;
import au.gov.dva.sopapi.sopref.data.servicedeterminations.StoredServiceDetermination;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.time.*;

public class ServiceDeterminationsTests {

    @Test
    public void convertWarlikeDeterminationToText() throws IOException {
        URL inputPdf = Resources.getResource("F2016L00994.pdf");
        byte[] pdfBytes = Resources.toByteArray(inputPdf);
        String result = Conversions.pdfToPlainText(pdfBytes);
        System.out.print(result);
    }

    @Test
    public void deserializeWarlike() throws IOException {

        URL inputPdf = Resources.getResource("serviceDeterminations/F2016L00994.json");
        String s = Resources.toString(inputPdf, Charsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        ServiceDetermination result = StoredServiceDetermination.fromJson(objectMapper.readTree(s));
        Assert.assertTrue(result != null);
        // round trip
        System.out.print(
                TestUtils.prettyPrint(StoredServiceDetermination.toJson(result)));
    }

    @Test
    public void deserializeOperation() throws IOException {
        String opJsonString = "{\n" +
                "    \"name\" : \"Slipper\",\n" +
                "    \"startDate\" : \"2001-10-11\",\n" +
                "    \"endDate\" : \"2009-07-29\",\n" +
                "    \"type\" : \"warlike\"\n" +
                "  },";

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(opJsonString);

        Operation result = StoredOperation.fromJson(jsonNode);

        ZonedDateTime expectedEndDateMidnightNextDay = ZonedDateTime.of(LocalDate.of(2009,7,30), LocalTime.MIDNIGHT, ZoneId.of("Australia/ACT"));
        Assert.assertTrue(result.getEndDate().get().isEqual(expectedEndDateMidnightNextDay.toOffsetDateTime()));
    }
}
