package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.Recommendation;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.google.common.collect.ImmutableList;

import java.util.Optional;
import java.util.function.Predicate;


public interface ProcessingRule {

    Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace);


    default Recommendation inferRecommendation(ImmutableList<FactorWithSatisfaction> factors, ServiceHistory serviceHistory, SoP applicableSop, Condition condition, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        // Generate the recommendation
        boolean satisfied = factors.stream().anyMatch(f -> f.isSatisfied());

        Recommendation recommendation;
        if (applicableSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis) {
            if (satisfied) recommendation = Recommendation.APPROVED;
            else if (caseTrace.getActualCftsDays().orElse(0) >= caseTrace.getRequiredCftsDaysForBop().orElse(Integer.MAX_VALUE)) recommendation = Recommendation.CHECK_RH_BOP_MET;
            else recommendation = Recommendation.CHECK_RH;
        }
        else {
            if (satisfied && caseTrace.getActualOperationalDays().orElse(0) > 0) recommendation = Recommendation.CHECK_RH_BOP_MET;
            else if (satisfied) recommendation = Recommendation.APPROVED;
            else recommendation = Recommendation.REJECT;
        }

        return recommendation;

    }

}

