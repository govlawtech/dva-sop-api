package au.gov.dva.sopapi.interfaces;

import au.gov.dva.sopapi.dtos.MilitaryOperation;
import au.gov.dva.sopapi.dtos.Recommendation;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopsupport.Act;
import au.gov.dva.sopapi.exceptions.DvaSopApiRuntimeException;
import au.gov.dva.sopapi.exceptions.SopParserRuntimeException;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.Operations;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import au.gov.dva.sopapi.veaops.Facade;
import au.gov.dva.sopapi.veaops.Facade$;
import au.gov.dva.sopapi.veaops.interfaces.VeaDeterminationOccurance;
import au.gov.dva.sopapi.veaops.interfaces.VeaOperationalServiceRepository;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public interface ProcessingRule {

    Optional<SoP> getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational, CaseTrace caseTrace);

    ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory, CaseTrace caseTrace);

    default Recommendation inferRecommendation(ImmutableList<FactorWithSatisfaction> factors, ServiceHistory serviceHistory, SoP applicableSop, Condition condition, Predicate<Deployment> isOperational, CaseTrace caseTrace) {

        // Generate the recommendation
        boolean satisfied = factors.stream().anyMatch(f -> f.isSatisfied());

        Recommendation recommendation;
        if (applicableSop.getStandardOfProof() == StandardOfProof.ReasonableHypothesis) {
            if (satisfied) recommendation = Recommendation.APPROVED;
            // no satisfied factors but standard of proof is RH
            else if (caseTrace.getActualCftsDays().orElse(0) >= caseTrace.getRequiredCftsDaysForBop().orElse(Integer.MAX_VALUE)) recommendation = Recommendation.CHECK_RH_BOP_MET;  // where the standard of proof is RH, but there are not enough days for that.  So we test if the number of days is greater than the BoP reqs.  There is still a chance for an 'upgrade' to RH.
            else recommendation = Recommendation.CHECK_RH;  // The standard of proof is RH, but there are not enough days
        }
        else { // standard of proof is BoP
            if (satisfied && caseTrace.getActualOperationalDays().orElse(0) > 0) recommendation = Recommendation.CHECK_RH_BOP_MET;
            else if (satisfied) recommendation = Recommendation.APPROVED;
            else recommendation = Recommendation.REJECT;
        }

        return recommendation;

    }


    default ImmutableList<MilitaryOperation> inferRelevantOperations(ServiceHistory serviceHistory, Condition condition, Repository repository,  Predicate<Deployment> isOperational, CaseTrace caseTrace)
    {
        // get test interval
        // get operations in that interval
        // pass in data on mrca or VEA operations

        Interval relevantInterval = caseTrace.getTestInterval();
        if (relevantInterval == null)
        {
            throw new DvaSopApiRuntimeException("Relevant interval not set in case trace.");
        }
        List<Deployment> operationDeployments =
                ProcessingRuleFunctions.getCFTSDeployments(serviceHistory)
                .stream().filter(isOperational)
                .collect(Collectors.toList());

        Act applicableAct = ProcessingRuleFunctions.InferApplicableAct(serviceHistory,condition);
        if (applicableAct == Act.Vea)
        {
            Optional<VeaOperationalServiceRepository> veaOperationalServiceRepositoryOpt = repository.getVeaOperationalServiceRepository();
            if (!veaOperationalServiceRepositoryOpt.isPresent())
            {
                throw new DvaSopApiRuntimeException("Must have VEA service determinations reference data.");
            }
            else {
                VeaOperationalServiceRepository veaOperationalServiceRepository = veaOperationalServiceRepositoryOpt.get();
                List<VeaDeterminationOccurance> matchingOperations = Facade.getMatchingOperationsForDeployments(operationDeployments, veaOperationalServiceRepository);


            }

        }

        // find corresponding declared Operations
        // need to determine case




    }

}

