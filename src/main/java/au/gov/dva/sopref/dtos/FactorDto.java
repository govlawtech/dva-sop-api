package au.gov.dva.sopref.dtos;

import au.gov.dva.sopref.interfaces.model.Factor;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static FactorDto fromFactor(Factor factor)
    {
        return new FactorDto(factor.getParagraph(),factor.getText(),
                factor.getDefinedTerms().stream().map(t -> DefinedTermDto.fromDefinedTerm(t)).collect(Collectors.toList()));

    }
}
