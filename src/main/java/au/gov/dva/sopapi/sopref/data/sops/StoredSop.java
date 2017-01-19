package au.gov.dva.sopapi.sopref.data.sops;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.exceptions.RepositoryError;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.data.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

public class StoredSop implements SoP, HasSchemaVersion {


    private static final Integer SCHEMA_VERSION = 1;

    @Override
    public Integer getSchemaVersion() {
        return SCHEMA_VERSION;
    }

    private static class Labels {
        public static final String SCHEMA_VERSION_LABEL = "schemaVersion";
        public static final String REGISTER_ID = "registerId";
        public static final String INSTRUMENT_NUMBER = "instrumentNumber";
        public static final String CITATION = "citation";
        public static final String CONDITION_NAME = "conditionName";
        public static final String EFFECTIVE_FROM = "effectiveFrom";
        public static final String END_DATE = "endDate";
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
    private final ImmutableSet<ICDCode> icdCodes;
    @Nonnull
    private final String conditionName;
    private Optional<LocalDate> endDate;

    public StoredSop(@Nonnull String registerId, @Nonnull InstrumentNumber instrumentNumber, @Nonnull String citation, @Nonnull ImmutableList<Factor> onsetFactors, @Nonnull ImmutableList<Factor> aggravationFactors, @Nonnull LocalDate effectiveFromDate, @Nonnull StandardOfProof standardOfProof, @Nonnull ImmutableSet<ICDCode> icdCodes, @Nonnull String conditionName, @Nonnull Optional<LocalDate> endDate) {
        this.registerId = registerId;
        this.instrumentNumber = instrumentNumber;
        this.citation = citation;
        this.onsetFactors = onsetFactors;
        this.aggravationFactors = aggravationFactors;
        this.effectiveFromDate = effectiveFromDate;
        this.standardOfProof = standardOfProof;
        this.icdCodes = icdCodes;

        this.conditionName = conditionName;
        this.endDate = endDate;
    }

    public static SoP fromJson(JsonNode jsonNode)
    {
        Integer schema = jsonNode.findValue(Labels.SCHEMA_VERSION_LABEL).asInt();
        if (!schema.equals(SCHEMA_VERSION))
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
                icdCodeListFromJsonArray(jsonNode.findPath(Labels.ICD_CODES)),
                jsonNode.findValue(Labels.CONDITION_NAME).asText(),
                jsonNode.hasNonNull(Labels.END_DATE) ? Optional.of(LocalDate.parse(jsonNode.findValue(Labels.END_DATE).asText(),DateTimeFormatter.ISO_LOCAL_DATE)) : Optional.empty()
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
    public Optional<LocalDate> getEndDate() {
        return endDate;
    }

    @Override
    public StandardOfProof getStandardOfProof() {
        return standardOfProof;
    }

    @Override
    public ImmutableSet<ICDCode> getICDCodes() {
        return icdCodes;
    }

    @Override
    public String getConditionName() {
        return conditionName;
    }


    @Override
    public String getRegisterId() {

        return registerId;
    }

    public static SoP withEndDate(SoP sopToEndDate, LocalDate endDate)
    {
        return new StoredSop(sopToEndDate.getRegisterId(),sopToEndDate.getInstrumentNumber(),sopToEndDate.getCitation(),sopToEndDate.getOnsetFactors(),
                sopToEndDate.getAggravationFactors(),sopToEndDate.getEffectiveFromDate(),
                sopToEndDate.getStandardOfProof(),sopToEndDate.getICDCodes(),sopToEndDate.getConditionName(),Optional.of(endDate));
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
       return JsonUtils.getChildrenOfArrayNode(jsonNode);
    }

    public static JsonNode toJson(SoP sop)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put(Labels.SCHEMA_VERSION_LABEL,SCHEMA_VERSION);
        rootNode.put(Labels.REGISTER_ID,sop.getRegisterId());
        rootNode.put(Labels.INSTRUMENT_NUMBER, formatInstrumentNumber(sop.getInstrumentNumber()));
        rootNode.put(Labels.CITATION,sop.getCitation());
        rootNode.put(Labels.CONDITION_NAME,sop.getConditionName());
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

    public static SoP repealed(LocalDate endDate, String repealingInstrument)
    {
        return null;
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

    public static String formatInstrumentNumber(InstrumentNumber instrumentNumber)
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

    private static ImmutableSet<ICDCode> icdCodeListFromJsonArray(JsonNode jsonNode)
    {
        assert (jsonNode.isArray());

        ImmutableSet<ICDCode> icdCodes = ImmutableSet.copyOf(getChildrenOfArrayNode(jsonNode).stream().map(jsonNode1 -> fromIcdCodeJsonObject(jsonNode1)).collect(Collectors.toList()));

        return icdCodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        StoredSop storedSop = (StoredSop) o;

        if (!this.registerId.equals(storedSop.registerId)) return false;
        if (!this.instrumentNumber.equals(storedSop.instrumentNumber)) return false;
        if (!this.citation.equals(storedSop.citation)) return false;
        if (!this.effectiveFromDate.equals(storedSop.effectiveFromDate)) return false;
        return this.standardOfProof == storedSop.standardOfProof;
    }

    @Override
    public int hashCode() {
        int result = this.registerId.hashCode();
        result = 31 * result + this.instrumentNumber.hashCode();
        result = 31 * result + this.citation.hashCode();
        result = 31 * result + this.effectiveFromDate.hashCode();
        result = 31 * result + this.standardOfProof.hashCode();
        return result;
    }
}

