package au.gov.dva.sopapi.sopref.data;

import au.gov.dva.sopapi.exceptions.ConversionToPlainTextRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Conversions {


    private static ImmutableList<String> compilationsWithKnownShortFootnote = ImmutableList.of(
          "F2015C00914",
           "F2015C00915",
            "F2016C00252",
            "F2016C00253",
            "F2016C00269",
            "F2016C00270",
            "F2016C00274",
            "F2016C00276",
            "F2016C00279",
            "F2016C00280"
    );

    private static ImmutableList<String> compilationsWithKnownMedFoodNote = ImmutableList.of(
            "F2017C00764",
            "F2017C00832",
            "F2017C00783",
            "F2017C00827"
    );


    public static String croppedPdfToPlaintext(byte[] pdf, String registerId) throws IOException {
        float footNoteHeight = inferFootNoteHeightFromRegisterId(registerId);
        RandomAccessBuffer randomAccessBuffer = new RandomAccessBuffer(pdf);
        PDFParser pdfParser = new PDFParser(randomAccessBuffer);
        pdfParser.parse();
        COSDocument cosDocument = pdfParser.getDocument();
        PDDocument pdDocument = new PDDocument(cosDocument);
        PDFTextStripperByArea pdfTextStripper = new PDFTextStripperByArea();

        pdfTextStripper.setSortByPosition(true);
        String regionName = "exclFootnotes";
        pdfTextStripper.addRegion(regionName, createCroppedJavaRectangle(footNoteHeight));

        StringBuilder sb = new StringBuilder();
        int numberOfPages = pdDocument.getNumberOfPages();
        for (int i = 0; i < numberOfPages; i++) {
            PDPage pdPage = pdDocument.getPage(i);
            pdfTextStripper.extractRegions(pdPage);
            String pageText = pdfTextStripper.getTextForRegion(regionName);
            sb.append(pageText);
        }

        pdDocument.close();
        randomAccessBuffer.close();
        cosDocument.close();

        return sb.toString();
    }



    public static String pdfToPlainText(byte[] pdf) throws IOException {
        RandomAccessBuffer randomAccessBuffer = new RandomAccessBuffer(pdf);
        PDFParser pdfParser = new PDFParser(randomAccessBuffer);
        pdfParser.parse();
        COSDocument cosDocument = pdfParser.getDocument();
        PDDocument pdDocument = new PDDocument(cosDocument);
        PDFTextStripper pdfTextStripper = new PDFTextStripper();
        String text = pdfTextStripper.getText(pdDocument);
        pdDocument.close();
        randomAccessBuffer.close();
        cosDocument.close();
        return text;
    }


    public static String wordDocToPlainText(byte[] worddoc, boolean isOldWordFormat) throws IOException {
        BodyContentHandler handler = new BodyContentHandler();

        Parser parser;
        if (isOldWordFormat) {
            parser = new OfficeParser();
        } else {
            parser = new OOXMLParser();
        }

        Metadata metadata = new Metadata();
        InputStream inputStream = new ByteArrayInputStream(worddoc);
        ParseContext parseContext = new ParseContext();
        try {
            parser.parse(inputStream, handler, metadata, parseContext);
        } catch (SAXException | TikaException e) {
            throw new ConversionToPlainTextRuntimeException(e);
        }
        return handler.toString();
    }


    public static String wordDocToPlainTextWithApachePOI(byte[] wordDoc, boolean isOldWordFormat) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(wordDoc);
        if (isOldWordFormat) {
            WordExtractor wordExtractor = new WordExtractor(inputStream);
            return wordExtractor.getText();
        } else {
            XWPFDocument xwpfDocument = new XWPFDocument(inputStream);
            XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(xwpfDocument);
            return xwpfWordExtractor.getText();
        }
    }


    public static String toString(JsonNode jsonNode) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    public static float inferFootNoteHeightFromRegisterId(String registerdId) {

        float heightInPointsOfTallFootNoteForCompilations = 124f;
        float heightInPointsOfMediumFootnoteForCompilations = 102f;
        float heightInPointsOfShortFootnoteForCompilations = 60f;

        float heightInPointsForTallFootnoteForPrimaryInstrument = 102f;
        float heightInPointsForShortFootnoteForPrimaryInstment = heightInPointsOfShortFootnoteForCompilations;

        RegisterIdInfo registerIdInfo = unpackRegisterId(registerdId);
        if (registerIdInfo.isCompilation()) {

            if (compilationsWithKnownShortFootnote.contains(registerdId))
            {
                return heightInPointsOfShortFootnoteForCompilations;
            }
            if (compilationsWithKnownMedFoodNote.contains(registerdId))
            {
                return heightInPointsOfMediumFootnoteForCompilations;
            }
            if (registerIdInfo.getYear() <= 2014) {
                return heightInPointsOfShortFootnoteForCompilations;
            }
            if (registerIdInfo.getYear() <= 2016)
            {
                return heightInPointsOfMediumFootnoteForCompilations;
            }

            else return heightInPointsOfTallFootNoteForCompilations;

        } else {
            if (registerIdInfo.getYear() < 2015) return heightInPointsForShortFootnoteForPrimaryInstment;
            if (registerIdInfo.getYear() == 2015) {
                if (registerIdInfo.getNumber() > 658) return heightInPointsForTallFootnoteForPrimaryInstrument;
                else return heightInPointsForShortFootnoteForPrimaryInstment;
            }
            else return heightInPointsForTallFootnoteForPrimaryInstrument;
        }

    }

    private static String regexForRegisterId = "F(20[0-9]{2})([LC])([0-9]{5})";
    private static Pattern patternForRegisterId = Pattern.compile(regexForRegisterId);

    private static RegisterIdInfo unpackRegisterId(String registerId) {
        Matcher matcher = patternForRegisterId.matcher(registerId);
        if (!matcher.matches()) {
            throw new ConversionToPlainTextRuntimeException("Cannot parse this register id: " + registerId);
        }

        int year = Integer.parseInt(matcher.group(1));
        boolean isCompilation = matcher.group(2).contentEquals("C");
        int number = Integer.parseInt(matcher.group(3));
        return new RegisterIdInfo(year, isCompilation, number);
    }

    private static class RegisterIdInfo {
        private final int year;
        private final boolean isCompilation;
        private final int number;

        public RegisterIdInfo(int year, boolean isCompilation, int number) {

            this.year = year;
            this.isCompilation = isCompilation;
            this.number = number;
        }

        public int getYear() {
            return year;
        }

        public boolean isCompilation() {
            return isCompilation;
        }

        public int getNumber() {
            return number;
        }
    }

    public static byte[] cropPdfToExcludeFootnotes(byte[] pdf, float footnoteHeightInPoints) throws IOException {

        PDDocument pdDocument = PDDocument.load(pdf);
        int numberOfPages = pdDocument.getNumberOfPages();
        // todo: assert page size is A4

        for (int i = 0; i < numberOfPages; i++) {
            PDPage pdPage = pdDocument.getPage(i);
            PDRectangle mediaBox = pdPage.getMediaBox();
            PDRectangle a4Size = PDRectangle.A4;
            PDRectangle boxSlightlyLargerThanA4 = new PDRectangle(0, 0, a4Size.getWidth() + 1, a4Size.getHeight() + 1);
            assert (mediaBox.getHeight() <= boxSlightlyLargerThanA4.getHeight());  // A4 only
            pdPage.setMediaBox(createCroppedRectangle(footnoteHeightInPoints));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        pdDocument.save(outputStream);
        pdDocument.close();
        byte[] croppedPdfBytes = outputStream.toByteArray();
        return croppedPdfBytes;
    }

    private static PDRectangle createCroppedRectangle(float footnoteHeightInPoints) {

        PDRectangle pdRectangle = PDRectangle.A4;
        // origin 0,0 is in lower left corner

        return new PDRectangle(0,
                footnoteHeightInPoints,
                pdRectangle.getWidth(),
                pdRectangle.getHeight() - footnoteHeightInPoints);

    }

    private static Rectangle2D createCroppedJavaRectangle(float footnoteHeightInPoints) {

        PDRectangle pdRectangle = PDRectangle.A4;
        // anchor coord is at lower left for PdRectangle, but upper left for Rectangle
        float upperLeftX = 0f;
        float upperLeftY = 0f;
        float width = pdRectangle.getWidth();
        float height = pdRectangle.getHeight() - footnoteHeightInPoints;
        Rectangle2D r = new Rectangle2D.Float(upperLeftX, upperLeftY, width, height);
        return r;

    }


}

