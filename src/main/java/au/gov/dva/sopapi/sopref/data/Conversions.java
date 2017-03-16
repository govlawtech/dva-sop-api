package au.gov.dva.sopapi.sopref.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.nio.charset.Charset;

public class Conversions {



    public static String pdfToPlainText(byte[] pdf) throws IOException {

        RandomAccessBuffer randomAccessBuffer = new RandomAccessBuffer(pdf);
        PDFParser pdfParser = new PDFParser(randomAccessBuffer);
        pdfParser.parse();
        COSDocument cosDocument = pdfParser.getDocument();
        PDDocument pdDocument = new PDDocument(cosDocument);
        PDFTextStripper pdfTextStripper = new PDFTextStripper();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        Writer outWriter = new OutputStreamWriter(outStream, Charsets.UTF_8);
        pdfTextStripper.writeText(pdDocument,outWriter);

        String text = outStream.toString("UTF-8");

        pdDocument.close();
        randomAccessBuffer.close();
        cosDocument.close();
        outStream.close();
        return text;
    }

    public static String toString(JsonNode jsonNode) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }


}

