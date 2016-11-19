package au.gov.dva.sopref.data;

import au.gov.dva.sopref.interfaces.model.SoP;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;

public class Conversions {
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

    public static SoP fromJson(JsonNode jsonNode){
        return null;
    }

    public static JsonNode toJson(SoP sop)
    {
        return null;
    }
}
