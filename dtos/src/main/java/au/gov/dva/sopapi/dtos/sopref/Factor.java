package au.gov.dva.sopapi.dtos.sopref;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Factor {

    @JsonProperty("paragraph")
    private final String _paragraph;

    @JsonProperty("text")
    private final String _text;

    @JsonProperty("definedTerms")
    private final List<DefinedTerm> _definedTerms;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Factor(@JsonProperty("paragraph") String paragraph, @JsonProperty("text") String text, @JsonProperty("definedTerms") List<DefinedTerm> definedTerms) {

        _paragraph = paragraph;
        _text = text;
        _definedTerms = definedTerms;
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
