package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.ConditionConfiguration;
import au.gov.dva.sopapi.interfaces.model.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.stream.Collectors;

public class OsteoarthritisRule extends LumbarSpondylosisRule {
    public OsteoarthritisRule(ConditionConfiguration conditionConfiguration) {

        super(conditionConfiguration);
    }


    // hard-coded quick fix for Feb to March deploy only!
    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        ImmutableList<FactorWithSatisfaction> withFullFactors = super.getSatisfiedFactors(condition, applicableSop, serviceHistory, caseTrace);

        ImmutableList<FactorWithSatisfaction> withSubFactorsInserted = withFullFactors.stream()
                .map(ff -> {
                    if (ff.isSatisfied() && ff.getFactor().getParagraph().contentEquals("9(14)")) {

                        if (applicableSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis) {

                            Factor replacementRhFactor = new Factor() {
                                @Override
                                public String getParagraph() {
                                    return "9(14)(b)";
                                }

                                @Override
                                public String getText() {
                                    return "lifting loads of at least 20 kilograms while bearing weight through the affected joint to a cumulative total of at least 100 000 kilograms within any ten year period before the clinical onset of osteoarthritis in that joint";
                                }

                                @Override
                                public ImmutableSet<DefinedTerm> getDefinedTerms() {
                                    return ff.getFactor().getDefinedTerms();
                                }
                            };

                            FactorWithSatisfaction replacementRhFactorWithSatisfaction = new FactorWithSatisfaction() {
                                @Override
                                public Factor getFactor() {
                                    return replacementRhFactor;
                                }

                                @Override
                                public Boolean isSatisfied() {
                                    return true;
                                }
                            };

                            return replacementRhFactorWithSatisfaction;
                        }
                        else {
                            Factor replacementBopFactor = new Factor() {
                                @Override
                                public String getParagraph() {
                                    return "9(14)(b)";
                                }

                                @Override
                                public String getText() {
                                    return "lifting loads of at least 20 kilograms while bearing weight through the affected joint:\r\n" +
                                            "(i) to a cumulative total of at least 150 000 kilograms within any ten year period before the clinical onset of osteoarthritis in that joint; and\r\n" +
                                            "(ii) where the clinical onset of osteoarthritis in that joint occurs within the 25 years following that period";
                                }

                                @Override
                                public ImmutableSet<DefinedTerm> getDefinedTerms() {
                                    return ff.getFactor().getDefinedTerms();
                                }
                            };

                            FactorWithSatisfaction replacementBopFactorWithSatisfaction = new FactorWithSatisfaction() {
                                @Override
                                public Factor getFactor() {
                                    return replacementBopFactor;
                                }

                                @Override
                                public Boolean isSatisfied() {
                                    return true;
                                }
                            };

                            return replacementBopFactorWithSatisfaction;
                        }
                    }
                    return ff;
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

        return withSubFactorsInserted;
    }
}