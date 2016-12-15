package au.gov.dva.sopapi.dtos.sopref;

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



}
