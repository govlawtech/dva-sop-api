package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.model.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.function.Predicate;

public class LumbarSpondylosisRule implements ProcessingRule {

    // todo: CFTS check

    public LumbarSpondylosisRule() {
    }

    @Override
    public ImmutableSet<String> appliesToInstrumentIds() {
        return ImmutableSet.of("F2014L00933","F2014L00930");
    }

    @Override
    public SoP getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational) {

        long daysOfOperationalService = ProcessingRuleFunctions.getNumberOfDaysOfOperationalServiceInInterval(
                condition.getStartDate().minusYears(10),condition.getStartDate(),
                ProcessingRuleFunctions.getDeployments(serviceHistory),
                isOperational);

        Rank rank = ProcessingRuleFunctions.getRankProximateToDate(serviceHistory.getServices(),condition.getStartDate());

        Integer minimumRequiredDaysOfOperationalServiceForRank = getMinDaysOfOperationalServiceForRH(rank);

        if (minimumRequiredDaysOfOperationalServiceForRank.longValue() <= daysOfOperationalService)
        {
            return condition.getSopPair().getRhSop();
        }
        else return condition.getSopPair().getBopSop();

    }


    @Override
    public ImmutableSet<Factor> getApplicableFactors(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational) {

        // call generic rule based on one day of continuous full time service
        return null;
    }

    @Override
    public ImmutableSet<Factor> getSatisfiedFactors(Condition condition, ServiceHistory serviceHistory) {
        return null;
    }

    private static Integer getMinDaysOfOperationalServiceForRH(Rank rank)
    {
        return ImmutableMap.of(
                Rank.Officer,23,
                Rank.OtherRank,26,
                Rank.SpecialForces,4,
                Rank.Unknown,26).get(rank);
    }
}