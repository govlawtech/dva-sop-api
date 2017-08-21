package au.gov.dva.dvasopapi.tests;


import au.gov.dva.dvasopapi.tests.mocks.MockLumbarSpondylosisSopRH;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.data.Conversions;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.UUID;


public class ConversionTests {


    private static Path createTempfile(String ext) throws IOException {
        assert(ext.startsWith("."));
        Path tmp = Files.createTempFile(UUID.randomUUID().toString(),ext);
        return tmp;
    }


    private static String testExtractTextWithCropping(String resourcePath,String registerId) throws IOException {
        URL inputPdf = Resources.getResource(resourcePath);
        byte[] pdfBytes = Resources.toByteArray(inputPdf);
        String result = Conversions.croppedPdfToPlaintext(pdfBytes,registerId);
        System.out.println(result);
        return result;
    }


    private static void testPdfCrop(String resourcePath, float footNoteheight) throws IOException {
        URL inputPdf = Resources.getResource(resourcePath);
        byte[] pdfBytes = Resources.toByteArray(inputPdf);
        byte[] cropped = Conversions.cropPdfToExcludeFootnotes(pdfBytes,footNoteheight);
        Path tmp = createTempfile(".pdf");
        Files.write(tmp,cropped,StandardOpenOption.APPEND);
        System.out.println("Cropped output for " + resourcePath + " at " + tmp.toString());
    }

    @Test
    public void pdfToText() throws IOException {
        URL inputPdf = Resources.getResource("F2016L00994.pdf");
        byte[] pdfBytes = Resources.toByteArray(inputPdf);
        String result = Conversions.pdfToPlainText(pdfBytes);
        int lineCount = result.split("[\r\n]").length;
        System.out.print(result);
        Assert.assertTrue(lineCount > 250);
    }


    @Test
    public void correctFootnoteHeightForServiceDeterminations() throws IOException {

        float footNoteHeight = Conversions.inferFootNoteHeightFromRegisterId("F2016L00994");
        Assert.assertTrue(footNoteHeight > 90f);
    }


    @Test
    public void testCropTallFootnote() throws IOException {
        testPdfCrop("allSops/F2016L00008.pdf",91f);
    }

    @Test
    public void cropPdfWithTallFootNote() throws IOException {

        String result = testExtractTextWithCropping("F2016L00994.pdf","F2016L00994");
        Assert.assertTrue(!result.contains("Authorised Version F2016L00994"));
    }

    @Test
    public void cropFootnoteFrom2017Haemorrhoids() throws IOException {
        String result = testExtractTextWithCropping("allSops/F2017L00005.pdf","F2017L00005");
        Assert.assertTrue(!result.contains("Authorised Version F2017L00005 registered 04/01/2017"));

    }

    @Test
    public void crop2017CompilationPdf() throws IOException {
        String result = testExtractTextWithCropping("allSops/F2017C00077.pdf","F2017C00077");
        Assert.assertTrue(!result.contains("neoplasm of the oesophagus has occurred within 20 years of\n" +
                "cessation;\n" +
                "(No. 120 of 2015)\n" +
                "Veterans' Entitlements Act 1986"));
    }

    @Test
    public void cropPdfWithSmallFootNote() throws IOException {
     //   Assert.assertNotNull(testPdfCrop("allSops/F2008L02192.pdf"));
    }

    @Test
    public void docXToTextWithApacheTika() throws IOException {
        String result = testDocToTextWithApacheTika("sopsInWordDocForm/F2017C00077.docx",false);
        System.out.println(result);
    }

    @Test
     public void oldDoctoTextwithApachePOI() throws IOException {
        String result = testDocToTextWithApachePOI("sopsInWordDocForm/F2008L02192.doc",true);

        System.out.println(result);
    }

    @Test
    public void docXtoTextwithApachePOI() throws IOException {
        String result = testDocToTextWithApachePOI("sopsInWordDocForm/F2017C00077.docx",false);

        System.out.println(result);
    }

    private static String testDocToTextWithApacheTika(String resourcePath, boolean isOldWordFormat) throws IOException {
        URL input = Resources.getResource(resourcePath);
        byte[] docBytes = Resources.toByteArray(input);
        String results = Conversions.wordDocToPlainText(docBytes,isOldWordFormat);
        return results;
    }

    private static String testDocToTextWithApachePOI(String resourcePath, boolean isOldWordformat) throws IOException {
        URL input = Resources.getResource(resourcePath);
        byte[] docBytes = Resources.toByteArray(input);
        String results = Conversions.wordDocToPlainTextWithApachePOI(docBytes,isOldWordformat);
        return results;
    }


    @Test
    public void sopToJson() throws JsonProcessingException {
        SoP testData = new MockLumbarSpondylosisSopRH();
        JsonNode result = StoredSop.toJson(testData);
        System.out.print(TestUtils.prettyPrint(result));
        Assert.assertTrue(result.elements().hasNext());
    }

    @Test
    public void jsonToSop() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(Resources.getResource("storedSop.json"));
        SoP result = StoredSop.fromJson(jsonNode);

        // round trip
        System.out.print(TestUtils.prettyPrint(StoredSop.toJson(result)));
    }

    @Test
    public void jacksonEmptyArray() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree("{ \"key\": [] }");
        JsonNode values = jsonNode.findPath("key");
        Assert.assertTrue(values.size() == 0);
        Assert.assertTrue(values.isArray());
    }

     @Test
    public void jacksonPopulatedArray() throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree("{ \"key\": [{\"some\" : \"value\"}] }");
        JsonNode values = jsonNode.findPath("key");
        JsonNode element = values.get(0);

        for (Iterator<JsonNode> it = values.elements(); it.hasNext(); ) {
             JsonNode el = it.next();
             System.out.print(el);
         }
        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.isArray());
        Assert.assertTrue(element.isObject());
    }

    @Test
    public void producecleansedTextForLSBop() throws IOException {

            URL inputPdf = Resources.getResource("sops_bop/F2014L00930.pdf");
            byte[] pdfBytes = Resources.toByteArray(inputPdf);
            String result = Conversions.pdfToPlainText(pdfBytes);
            int lineCount = result.split("[\r\n]+").length;
            System.out.print(result);
            Assert.assertTrue(lineCount > 250);
    }


    @Test
    public void extractExponentInPdf() {
        // todo: make custom PDFTextStripper which detects superscript 2 and replaces it with unicode \u00B2
    }




}
