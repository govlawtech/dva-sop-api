package au.gov.dva.sopref.data.sops;

import au.gov.dva.sopref.exceptions.RepositoryError;
import au.gov.dva.sopref.interfaces.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class StoredSop implements SoP, HasSchemaVersion {


    private static final Double SCHEMA_VERSION = 1.0;

    @Override
    public Double getSchemaVersion() {
        return SCHEMA_VERSION;
    }

    private static class Labels {
        public static final String SCHEMA_VERSION_LABEL = "schemaVersion";
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
        public static final String ICD_CODES = "icdCodes";
        public static final String ICD_CODE_VERSION = "version";
        public static final String ICD_CODE = "code";
    }

    private final String registerId;
    private final InstrumentNumber instrumentNumber;
    private final String citation;
    private final ImmutableList<Factor> aggravationFactors;
    private final ImmutableList<Factor> onsetFactors;
    private final LocalDate effectiveFromDate;
    private final StandardOfProof standardOfProof;
    @Nonnull
    private final ImmutableList<ICDCode> icdCodes;

    public StoredSop(@Nonnull String registerId, @Nonnull InstrumentNumber instrumentNumber, @Nonnull String citation, @Nonnull ImmutableList<Factor> onsetFactors, @Nonnull ImmutableList<Factor> aggravationFactors, @Nonnull LocalDate effectiveFromDate, @Nonnull StandardOfProof standardOfProof, @Nonnull ImmutableList<ICDCode> icdCodes) {
        this.registerId = registerId;
        this.instrumentNumber = instrumentNumber;
        this.citation = citation;
        this.onsetFactors = onsetFactors;
        this.aggravationFactors = aggravationFactors;
        this.effectiveFromDate = effectiveFromDate;
        this.standardOfProof = standardOfProof;
        this.icdCodes = icdCodes;
    }

    public static SoP fromJson(JsonNode jsonNode)
    {
        Double schema = jsonNode.findValue(Labels.SCHEMA_VERSION_LABEL).asDouble();
        if (schema.doubleValue() != SCHEMA_VERSION)
        {
            throw new RepositoryError(String.format("Json schema %d does not match expected value of %d", schema, SCHEMA_VERSION));
        }

        return new StoredSop(
                jsonNode.findValue(Labels.REGISTER_ID).asText(),
                fromInstrumentNumberJsonValue(jsonNode.findValue(Labels.INSTRUMENT_NUMBER).asText()),
                jsonNode.findValue(Labels.CITATION).asText(),
                factorListFromJsonArray(jsonNode.findPath(Labels.ONSET_FACTORS)),
                factorListFromJsonArray(jsonNode.findPath(Labels.AGGRAVATION_FACTORS)),
                LocalDate.parse(jsonNode.findValue(Labels.EFFECTIVE_FROM).asText()),
                StandardOfProof.fromString(jsonNode.findValue(Labels.STANDARD_OF_PROOF).asText()),
                icdCodeListFromJsonArray(jsonNode.findPath(Labels.ICD_CODES))
                );

    }


    private static ImmutableList<Factor> factorListFromJsonArray(JsonNode jsonNode)
    {
        assert (jsonNode.isArray());

        ImmutableList<Factor> factors = ImmutableList.copyOf(getChildrenOfArrayNode(jsonNode).stream().map(jsonNode1 -> factorFromJson(jsonNode1)).collect(Collectors.toList()));

        return factors;
    }




    @Override
    public ImmutableList<Factor> getAggravationFactors() {
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
    public ImmutableList<Factor> getOnsetFactors() {
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
    public ImmutableList<ICDCode> getICDCodes() {
        return icdCodes;
    }


    @Override
    public String getRegisterId() {

        return registerId;
    }


    private static Factor factorFromJson(JsonNode jsonNode) {

        ImmutableList<JsonNode> definedTermNodes = getChildrenOfArrayNode(jsonNode.findPath(Labels.DEFINED_TERMS));

        ImmutableSet<DefinedTerm> definedTerms = ImmutableSet.copyOf(definedTermNodes.stream().map(n -> definedTermFromJson(n)).collect(Collectors.toList()));
        
        return new StoredFactor(
                jsonNode.findValue(Labels.PARAGRAPH).asText(),
                jsonNode.findValue(Labels.TEXT).asText(), 
                definedTerms);
    }

    private static DefinedTerm definedTermFromJson(JsonNode jsonNode)
    {
        assert(jsonNode.has(Labels.TERM) && jsonNode.has(Labels.DEFINITION));
        return new StoredDefinedTerm(jsonNode.findValue(Labels.TERM).asText(),jsonNode.findValue(Labels.DEFINITION).asText());
    }

    private static ImmutableList<JsonNode> getChildrenOfArrayNode(JsonNode jsonNode)
    {
        assert (jsonNode.isArray());
        List<JsonNode> children = new ArrayList<>();

        for (Iterator<JsonNode> it = jsonNode.elements(); it.hasNext(); ) {
            JsonNode el = it.next();
            children.add(el);
        }

        return ImmutableList.copyOf(children);
    }

    public static JsonNode toJson(SoP sop)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put(Labels.SCHEMA_VERSION_LABEL,SCHEMA_VERSION);
        rootNode.put(Labels.REGISTER_ID,sop.getRegisterId());
        rootNode.put(Labels.INSTRUMENT_NUMBER, formatInstrumentNumber(sop.getInstrumentNumber()));
        rootNode.put(Labels.CITATION,sop.getCitation());
        rootNode.put(Labels.EFFECTIVE_FROM,sop.getEffectiveFromDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        rootNode.put(Labels.STANDARD_OF_PROOF,sop.getStandardOfProof().toString());

        ArrayNode icdCodesNode = rootNode.putArray(Labels.ICD_CODES);
        for (ICDCode icdCode : sop.getICDCodes())
            icdCodesNode.add(BasicICDCode.toJson(icdCode));


        ArrayNode onsetFactorsNode =  rootNode.putArray(Labels.ONSET_FACTORS);
        for (Factor factor : sop.getOnsetFactors())
            onsetFactorsNode.add(toJson(factor));

        ArrayNode aggravationfactorsNode = rootNode.putArray(Labels.AGGRAVATION_FACTORS);
        for (Factor factor : sop.getAggravationFactors())
            aggravationfactorsNode.add(toJson(factor));

        return rootNode;
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

    private static ICDCode fromIcdCodeJsonObject(JsonNode icdCode)
    {
         return new BasicICDCode(icdCode.findValue(Labels.ICD_CODE_VERSION).asText(),
                 icdCode.findValue(Labels.ICD_CODE).asText());
    }

    private static ImmutableList<ICDCode> icdCodeListFromJsonArray(JsonNode jsonNode)
    {
        assert (jsonNode.isArray());

        ImmutableList<ICDCode> icdCodes = ImmutableList.copyOf(getChildrenOfArrayNode(jsonNode).stream().map(jsonNode1 -> fromIcdCodeJsonObject(jsonNode1)).collect(Collectors.toList()));

        return icdCodes;
    }


}

