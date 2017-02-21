package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.interfaces.AccumulationRule;
import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.model.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Predicate;

public class LumbarSpondylosisRule implements ProcessingRule, AccumulationRule {

    private static Logger logger = LoggerFactory.getLogger(LumbarSpondylosisRule.class.getSimpleName());

    private static SimpleRuleSpec _simpleRuleSpec = new SimpleRuleSpec(
                new RankSpec(49,3726,23),
                new RankSpec(37,3280,26),
                new RankSpec(6,21267,4));

    public LumbarSpondylosisRule() {
    }

    @Override
    public ImmutableSet<String> appliesToInstrumentIds() {
        return ImmutableSet.of("F2014L00933","F2014L00930");
    }

    @Override
    public SoP getApplicableSop(Condition condition, ServiceHistory serviceHistory, Predicate<Deployment> isOperational) {

        OffsetDateTime startDateForPeriodOfOperationalService = condition.getStartDate().minusYears(10);
        OffsetDateTime endDateForPeriodOfOperationalService = condition.getStartDate();
        logger.trace("Determining applicable SoP...");
        logger.trace("Start date for the period of operational service: " +  startDateForPeriodOfOperationalService);
        logger.trace("End date for period of operational service: " + endDateForPeriodOfOperationalService);

        long daysOfOperationalService = ProcessingRuleFunctions.getNumberOfDaysOfOperationalServiceInInterval(
                startDateForPeriodOfOperationalService,endDateForPeriodOfOperationalService,
                ProcessingRuleFunctions.getDeployments(serviceHistory),
                isOperational);
        logger.trace("Days of operational service: " + daysOfOperationalService);

        Rank rank = ProcessingRuleFunctions.getRankProximateToDate(serviceHistory.getServices(),condition.getStartDate());

        Integer minimumRequiredDaysOfOperationalServiceForRank = getMinDaysOfOperationalServiceForRH(rank);

        if (minimumRequiredDaysOfOperationalServiceForRank.longValue() <= daysOfOperationalService)
        {
            return condition.getSopPair().getRhSop();
        }
        else return condition.getSopPair().getBopSop();
    }


    @Override
    public ImmutableList<FactorWithSatisfaction> getSatisfiedFactors(Condition condition, SoP applicableSop, ServiceHistory serviceHistory) {
        ImmutableList<Factor> applicableFactors =  condition.getApplicableFactors(applicableSop);

        Optional<Service> serviceDuringWhichConditionStarts =  ProcessingRuleFunctions.identifyServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(),condition.getStartDate());

        Rank relevantRank = serviceDuringWhichConditionStarts.get().getRank();

        Integer cftsDaysRequired = _simpleRuleSpec.getSpec(relevantRank).getRequiredWeeksCfts() * 7;

        Long actualDaysOfCfts = ProcessingRuleFunctions.getDaysOfContinuousFullTimeServiceToDate(serviceHistory,condition.getStartDate());
        if (actualDaysOfCfts >= cftsDaysRequired) {
            ImmutableList<FactorWithSatisfaction> inferredFactors =
                    ProcessingRuleFunctions.withSatsifiedFactors(applicableFactors, "6(j)", "6(y)");

            return inferredFactors;
        }
        else
        {
            return ProcessingRuleFunctions.withSatsifiedFactors(applicableFactors);
        }
    }


    private static Integer getMinDaysOfOperationalServiceForRH(Rank rank)
    {
       return _simpleRuleSpec.getSpec(rank).getRhDaysOfOpServiceInLast10Years();
    }


    @Override
    public Long getAccumulation() {
        return null;
    }

    @Override
    public String getAccumulationUnit() {
        return "kg";
    }
}