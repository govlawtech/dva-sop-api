package au.gov.dva.sopref.data.SoPs;

import au.gov.dva.sopref.data.Conversions;
import au.gov.dva.sopref.interfaces.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class StoredSop implements SoP {

    private static class Labels {
        public static final String REGISTER_ID = "registerId";
        public static final String INSTRUMENT_NUMBER = "instrumentNumber";
        public static final String CITATION = "citation";
        public static final String EFFECTIVE_FROM = "effectiveFrom";
        public static final String STANDARD_OF_PROOF = "standardOfProof";
        public static final String ONSET_FACTORS = "onsetFactors";
        public static final String AGGRAVATION_FACTORS = "aggravationFactors";
        public static final String PARAGRAPH = "paragraph";
        public static final String TEXT = "text";
        public static final String TERM = "term";
        public static final String DEFINITION = "definition";
        public static final String DEFINED_TERMS = "definedTerms";
    }
    
    private final String registerId;
    private final InstrumentNumber instrumentNumber;
    private final String citation;
    private final ImmutableSet<Factor> aggravationFactors;
    private final ImmutableSet<Factor> onsetFactors;
    private final LocalDate effectiveFromDate;
    private final StandardOfProof standardOfProof;

    public StoredSop(String registerId, InstrumentNumber instrumentNumber, String citation, ImmutableSet<Factor> onsetFactors, ImmutableSet<Factor> aggravationFactors, LocalDate effectiveFromDate, StandardOfProof standardOfProof) {
        this.registerId = registerId;
        this.instrumentNumber = instrumentNumber;
        this.citation = citation;
        this.onsetFactors = onsetFactors;
        this.aggravationFactors = aggravationFactors;
        this.effectiveFromDate = effectiveFromDate;
        this.standardOfProof = standardOfProof;
    }

    public static SoP fromJson(JsonNode jsonNode)
    {
        return new StoredSop(
                jsonNode.findValue(Labels.REGISTER_ID).asText(),
                fromInstrumentNumberJsonValue(jsonNode.findValue(Labels.INSTRUMENT_NUMBER).asText()),
                jsonNode.findValue(Labels.CITATION).asText(),
                ImmutableSet.copyOf(jsonNode.findValues(Labels.ONSET_FACTORS).stream()
                        .filter(jsonNode1 -> jsonNode1.elements().hasNext())
                        .map(jsonNode1 -> factorFromJson(jsonNode1)).collect(Collectors.toList())),
                ImmutableSet.copyOf(jsonNode.findValues(Labels.AGGRAVATION_FACTORS).stream()
                        .filter(jsonNode1 -> jsonNode1.elements().hasNext())
                        .map(jsonNode1 -> factorFromJson(jsonNode1)).collect(Collectors.toList())),
                LocalDate.parse(jsonNode.findValue(Labels.EFFECTIVE_FROM).asText()),
                fromStandardOfProofJsonValue(jsonNode.findValue(Labels.STANDARD_OF_PROOF).asText())

                );

    }



    @Override
    public ImmutableSet<Factor> getAggravationFactors() {
        return aggravationFactors;
    }

    @Override
    public InstrumentNumber getInstrumentNumber() {
        return instrumentNumber;
    }

    @Override
    public String getCitation() {
        return citation;
    }

    @Override
    public ImmutableSet<Factor> getOnsetFactors() {
        return onsetFactors;
    }

    @Override
    public LocalDate getEffectiveFromDate() {
        return effectiveFromDate;
    }

    @Override
    public StandardOfProof getStandardOfProof() {
        return standardOfProof;
    }


    @Override
    public String getRegisterId() {

        return registerId;
    }
   


    private static Factor factorFromJson(JsonNode jsonNode) {

        // bug todo: node is array, not factor object

        List<JsonNode> definedTermNodes = jsonNode.findValues(Labels.DEFINED_TERMS);
        List<DefinedTerm> definedTerms = definedTermNodes.stream().map(n -> definedTermFromJson(n)).collect(Collectors.toList());
        
        return new StoredFactor(
                jsonNode.findValue(Labels.PARAGRAPH).asText(),
                jsonNode.findValue(Labels.TEXT).asText(), 
                ImmutableSet.copyOf(definedTerms));
    }

    private static DefinedTerm definedTermFromJson(JsonNode jsonNode)
    {
        assert(jsonNode.has(Labels.TERM) && jsonNode.has(Labels.DEFINITION));
        return new StoredDefinedTerm(jsonNode.findValue(Labels.TERM).asText(),jsonNode.findValue(Labels.DEFINITION).asText());
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

    private static StandardOfProof fromStandardOfProofJsonValue(String value)
    {
        if (value.contentEquals("RH"))
            return StandardOfProof.ReasonableHypothesis;
        if (value.contentEquals("BoP"))
            return StandardOfProof.BalanceOfProbabilities;
        throw new IllegalArgumentException(String.format("Unrecognised standard of proof: %s", value));
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

    private static InstrumentNumber fromInstrumentNumberJsonValue(String value)
    {
        String[] parts = value.split("/");
        assert(parts.length == 2);
        return new InstrumentNumber() {
            @Override
            public int getNumber() {
                return Integer.parseInt(parts[0]);
            }

            @Override
            public int getYear() {
                return Integer.parseInt(parts[1]);
            }
        };
    }
}

