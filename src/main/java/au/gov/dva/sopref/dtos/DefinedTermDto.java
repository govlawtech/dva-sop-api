package au.gov.dva.sopref.dtos;

import au.gov.dva.interfaces.model.DefinedTerm;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefinedTermDto {

    @JsonProperty("term")
    private final String _term;

    @JsonProperty("definition")
    private final String _definition;

    @JsonCreator
    public DefinedTermDto(@JsonProperty("term") String term, @JsonProperty("definition") String definition)
    {
        _term = term;
        _definition = definition;
    }


    public static DefinedTermDto fromDefinedTerm(DefinedTerm definedTerm)
    {
        return new DefinedTermDto(definedTerm.getTerm(),definedTerm.getDefinition());
    }
}
