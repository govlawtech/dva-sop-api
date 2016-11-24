package au.gov.dva.sopref.data.SoPs;

import au.gov.dva.sopref.interfaces.model.DefinedTerm;

public class StoredDefinedTerm implements DefinedTerm {
    private final String term;

    @Override
    public String getTerm() {
        return term;
    }

    @Override
    public String getDefinition() {
        return definition;
    }

    private final String definition;

    public StoredDefinedTerm(String term, String definition)
    {

        this.term = term;
        this.definition = definition;
    }
}
