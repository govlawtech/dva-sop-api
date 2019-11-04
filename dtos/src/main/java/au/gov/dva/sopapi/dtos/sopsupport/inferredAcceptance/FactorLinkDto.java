package au.gov.dva.sopapi.dtos.sopsupport.inferredAcceptance;

import au.gov.dva.sopapi.dtos.sopref.DefinedTerm;
import au.gov.dva.sopapi.dtos.sopref.FactorDto;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;




public class FactorLinkDto {


    @JsonProperty("conditionVariant")
    private final String _conditionVariant;

    @JsonProperty("paragraph")
    private final String _paragraph;

    @JsonProperty("text")
    private final String _text;

    @JsonProperty("definedTerms")
    private final List<DefinedTerm> _definedTerms;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public FactorLinkDto(@JsonProperty("paragraph") String paragraph, @JsonProperty("text") String text, @JsonProperty("definedTerms") List<DefinedTerm> definedTerms, @JsonProperty("conditionVariant") String conditionVariant) {
        _paragraph = paragraph;
        _text = text;
        _definedTerms = definedTerms;
        _conditionVariant = conditionVariant;
    }


    public String get_paragraph() {
        return _paragraph;
    }

    public String get_text() {
        return _text;
    }

    public List<DefinedTerm> get_definedTerms() {
        return _definedTerms;
    }

}
