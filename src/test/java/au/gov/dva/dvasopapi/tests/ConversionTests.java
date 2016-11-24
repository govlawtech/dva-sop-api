package au.gov.dva.dvasopapi.tests;


import au.gov.dva.dvasopapi.tests.mocks.MockLumbarSpondylosisSop;
import au.gov.dva.sopref.data.Conversions;
import au.gov.dva.sopref.data.SoPs.StoredSop;
import au.gov.dva.sopref.interfaces.model.SoP;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class ConversionTests {

    @Test
    public void pdfToText() throws IOException {
        URL inputPdf = Resources.getResource("F2014L00933.pdf");
        byte[] pdfBytes = Resources.toByteArray(inputPdf);
        String result = Conversions.pdfToPlainText(pdfBytes);
        int lineCount = result.split("[\r\n]").length;
        System.out.print(result);
        Assert.assertTrue(lineCount > 250);
    }

    @Test
    public void sopToJson() throws JsonProcessingException {
        SoP testData = new MockLumbarSpondylosisSop();
        JsonNode result = StoredSop.toJson(testData);
        System.out.print(TestUtils.prettyPrint(result));
        Assert.assertTrue(result.elements().hasNext());
    }

    @Test
    public void jsonToSop() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(Resources.getResource("storedSop.json"));
        SoP result = StoredSop.fromJson(jsonNode);

    }
}
