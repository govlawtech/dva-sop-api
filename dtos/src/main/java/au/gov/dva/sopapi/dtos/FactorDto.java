package au.gov.dva.sopapi.dtos;

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

    @JsonCreator
    public FactorDto(@JsonProperty("paragraph") String paragraph,@JsonProperty("text") String text, List<DefinedTermDto> definedTermDtos) {

        _paragraph = paragraph;
        _text = text;
        _definedTerms = definedTermDtos;
    }


}
