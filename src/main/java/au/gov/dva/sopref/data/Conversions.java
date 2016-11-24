package au.gov.dva.sopref.data;

import au.gov.dva.sopref.interfaces.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class Conversions {

    private static class Labels {
        public static final String REGISTER_ID =  "registerId";
        public static final String INSTRUMENT_NUMBER =  "instrumentNumber";
        public static final String CITATION = "citation";
        public static final String EFFECTIVE_FROM = "effectiveFrom";
        public static final String STANDARD_OF_PROOF = "standardOfProof";
        public static final String ONSET_FACTORS =  "onsetFactors";
        public static final String AGGRAVATION_FACTORS = "aggravationFactors";
        public static final String PARAGRAPH = "paragraph";
        public static final String TEXT = "text";
        public static final String TERM = "term";
        public static final String DEFINITION = "definition";
        public static final String DEFINED_TERMS = "definedTerms";
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

    public static SoP fromJson(JsonNode jsonNode){
        return null;
    }

    public static JsonNode toJson(SoP sop)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put(Labels.REGISTER_ID,sop.getRegisterId());
        rootNode.put(Labels.INSTRUMENT_NUMBER, formatInstrumentNumber(sop.getInstrumentNumber()));
        rootNode.put(Labels.CITATION,sop.getCitation());
        rootNode.put(Labels.EFFECTIVE_FROM,sop.getEffectiveFromDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        rootNode.put(Labels.STANDARD_OF_PROOF,formatStandardOfProof(sop.getStandardOfProof()));

        ArrayNode onsetFactorsNode =  rootNode.putArray(Labels.ONSET_FACTORS);
        for (Factor factor : sop.getOnsetFactors())
            onsetFactorsNode.add(toJson(factor));

        ArrayNode aggravationfactorsNode = rootNode.putArray(Labels.AGGRAVATION_FACTORS);
        for (Factor factor : sop.getAggravationFactors())
            aggravationfactorsNode.add(toJson(factor));

        return rootNode;

    }

    private static String formatStandardOfProof(StandardOfProof standardOfProof)
    {
        switch (standardOfProof){
            case BalanceOfProbabilities: return "BoP";
            case ReasonableHypothesis: return "RH";
            default: throw new IllegalArgumentException(String.format("Unrecognised standard of proof: %s", standardOfProof));
        }

    }

    private static JsonNode toJson(Factor factor)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put(Labels.PARAGRAPH,factor.getParagraph());
        objectNode.put(Labels.TEXT,factor.getText());
        ArrayNode definedTermsArray =  objectNode.putArray(Labels.DEFINED_TERMS);
        for (DefinedTerm definedTerm : factor.getDefinedTerms())
            definedTermsArray.add(toJson(definedTerm));
        return objectNode;
    }

    private static JsonNode toJson(DefinedTerm definedTerm)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put(Labels.TERM,definedTerm.getTerm());
        objectNode.put(Labels.DEFINITION,definedTerm.getDefinition());
        return objectNode;
    }

    private static String formatInstrumentNumber(InstrumentNumber instrumentNumber)
    {
        return String.format("%d/%d", instrumentNumber.getNumber(), instrumentNumber.getYear());
    }
}
