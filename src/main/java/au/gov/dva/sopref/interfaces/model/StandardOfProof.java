package au.gov.dva.sopref.interfaces.model;

public enum StandardOfProof {

    ReasonableHypothesis("Reasonable Hypothesis"),
    BalanceOfProbabilities("Balance of Probabilities");

    @Override
    public String toString() {
        return text;
    }

    private String text;
    StandardOfProof(String text)
    {
        this.text = text;
    }

}
