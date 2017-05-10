package au.gov.dva.sopapi.sopref.data.sops;

import au.gov.dva.sopapi.interfaces.model.DefinedTerm;

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
