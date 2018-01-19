package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.sopref.DefinedTerm;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class FactorWithInferredResultDto {


    @JsonProperty("paragraph")
    private final String _paragraph;

    @JsonProperty("text")
    private final String _text;

    @JsonProperty("definedTerms")
    private final List<DefinedTerm> _definedTerms;

    @JsonProperty("satisfied")
    private final Boolean _satisfied;

    @JsonProperty("applicablePart")
    private final String _applicablePart;


    @JsonCreator
    public FactorWithInferredResultDto(@JsonProperty("paragraph") String paragraph, @JsonProperty("text") String text, @JsonProperty("definedTerms") List<DefinedTerm> definedTerms, @JsonProperty("satisfaction") Boolean satisfied, @JsonProperty("applicablePart") String applicablePart) {
        _paragraph = paragraph;
        _text = text;
        _definedTerms = definedTerms;
        _satisfied = satisfied;
        _applicablePart = applicablePart;
    }

    @JsonIgnore
    public String getParagraph() {
        return _paragraph;
    }

    @JsonIgnore
    public String getText() {
        return _text;
    }

    @JsonIgnore
    public Boolean getSatisfaction() {
        return _satisfied;
    }

    @JsonIgnore
    public ImmutableList<DefinedTerm> getDefinedTerms() {
        return ImmutableList.copyOf(_definedTerms);
    }

    @JsonIgnore
    public String getApplicablePart() {
        return _applicablePart;
    }
}
