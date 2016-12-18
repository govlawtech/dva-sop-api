package au.gov.dva.sopapi.dtos.sopref;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FactorDto {

    @JsonProperty("paragraph")
    private final String _paragraph;

    @JsonProperty("text")
    private final String _text;

    @JsonProperty("definedTerms")
    private final List<DefinedTermDto> _definedTerms;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public FactorDto(@JsonProperty("paragraph") String paragraph, @JsonProperty("text") String text, @JsonProperty("definedTerms") List<DefinedTermDto> definedTermDtos) {

        _paragraph = paragraph;
        _text = text;
        _definedTerms = definedTermDtos;
    }


}
