package au.gov.dva.sopapi.dtos.sopref;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefinedTerm {

    @JsonProperty("term")
    private final String _term;

    @JsonProperty("definition")
    private final String _definition;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DefinedTerm(@JsonProperty("term") String term, @JsonProperty("definition") String definition)
    {
        _term = term;
        _definition = definition;
    }



}
