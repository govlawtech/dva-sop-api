package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.Recommendation;
import au.gov.dva.sopapi.exceptions.ProcessingRuleRuntimeException;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.parsing.traits.ParaReferenceSplitter;
import au.gov.dva.sopapi.sopref.parsing.traits.SubParasHandler;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FactorSubPartDecorator extends ProcessingRuleBase implements ProcessingRule {


    private final ProcessingRule toDecorate;

    public FactorSubPartDecorator(ProcessingRule toDecorate, ConditionConfiguration conditionConfiguration)
    {
        super(conditionConfiguration);
        this.toDecorate = toDecorate;
    }


    @Override
    public Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        return toDecorate.getApplicableSop(condition,serviceHistory,isOperational,caseTrace);
    }

    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace) {

        ImmutableList<FactorWithSatisfaction> factors = toDecorate.getSatisfiedFactors(condition,applicableSop,serviceHistory,caseTrace);
        Optional<ApplicableRuleConfiguration> applicableRuleConfiguration = super.getApplicableRuleConfiguration(serviceHistory,condition,caseTrace);
        if (!applicableRuleConfiguration.isPresent()) throw new ProcessingRuleRuntimeException("Could not determine applicable rule configuration.");
        Optional<? extends RuleConfigurationItem> applicableRuleConfigurationItem = applicableRuleConfiguration.get().getRuleConfigurationForStandardOfProof(applicableSop.getStandardOfProof());
        if (!applicableRuleConfigurationItem.isPresent()) throw new ProcessingRuleRuntimeException("Could not determine applicable rule configuration item.");

        ImmutableList<FactorWithSatisfaction> addornedWithSubPart = factors.stream()
                .map(f -> addornWithApplicablePartIfAny(applicableRuleConfigurationItem.get(),f))
                .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));

        return addornedWithSubPart;

    }

    private FactorWithSatisfaction addornWithApplicablePartIfAny(RuleConfigurationItem ruleConfigurationItem, FactorWithSatisfaction toAddorn)
    {
        String mainFactorRef = toAddorn.getFactor().getParagraph();

        Optional<FactorReference> factorReferenceWithSubPart = ruleConfigurationItem.getFactorRefObjects().stream()
        .filter(factorReference -> factorReference.getMainFactorReference().contentEquals(mainFactorRef))
        .filter(factorReference -> factorReference.getFactorPartReference().isPresent())
        .findFirst();

        if (factorReferenceWithSubPart.isPresent())
        {
            String applicablePart = factorReferenceWithSubPart.get().getFactorPartReference().get();
            if (!factorContainsSubPart(toAddorn.getFactor(),applicablePart))
            {
                throw new ProcessingRuleRuntimeException(String.format("The subpart reference %s is not present in factor %s for instrument %s, condition %s", applicablePart, mainFactorRef, ruleConfigurationItem.getInstrumentId(), ruleConfigurationItem.getConditionName()));
            }

            return new SatisfiedFactorWithApplicablePart(toAddorn,applicablePart);
        }

        else {
            return toAddorn;
        }


    }

    private boolean factorContainsSubPart(Factor factor, String subPartReference)
    {
        if (!factor.getConditionVariant().isPresent()) return false;

        List<ConditionVariantFactor> variantFactors = factor.getConditionVariant().get()
                .getVariantFactors().stream()
                .filter(s -> s.getSubParagraph().contentEquals(subPartReference))
                .collect(Collectors.toList());

        return !variantFactors.isEmpty();

    }



    @Override
    public void attachConfiguredFactorsToCaseTrace(Condition condition, ServiceHistory serviceHistory, CaseTrace caseTrace) {
        toDecorate.attachConfiguredFactorsToCaseTrace(condition,serviceHistory,caseTrace);
    }

    @Override
    public Recommendation inferRecommendation(ImmutableList<FactorWithSatisfaction> factors, ServiceHistory serviceHistory, SoP applicableSop, Condition condition, Predicate<Deployment> isOperational, CaseTrace caseTrace) {
        return toDecorate.inferRecommendation(factors,serviceHistory,applicableSop,condition,isOperational,caseTrace);
    }
}
