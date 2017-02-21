package au.gov.dva.sopapi.dtos.sopsupport.components;

import au.gov.dva.sopapi.dtos.sopref.DefinedTerm;
import com.fasterxml.jackson.annotation.JsonCreator;
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




    @JsonCreator
    public FactorWithInferredResultDto(@JsonProperty("paragraph") String paragraph, @JsonProperty("text") String text, @JsonProperty("definedTerms") List<DefinedTerm> definedTerms, @JsonProperty("satisfaction") Boolean satisfied) {
        _paragraph = paragraph;
        _text = text;
        _definedTerms = definedTerms;
        _satisfied = satisfied;
    }

    public String getParagraph() {
        return _paragraph;
    }

    public String getText() {
        return _text;
    }

    public Boolean getSatisfaction() {
        return _satisfied;
    }

    public ImmutableList<DefinedTerm> getDefinedTerms() {
        return ImmutableList.copyOf(_definedTerms);
    }
}
