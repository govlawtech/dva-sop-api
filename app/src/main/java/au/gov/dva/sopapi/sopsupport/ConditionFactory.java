package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.exceptions.ProcessingRuleRuntimeException;
import au.gov.dva.sopapi.interfaces.ConditionConfiguration;
import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.AllDaysOfServiceSelector;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.FixedDaysPeriodSelector;
import au.gov.dva.sopapi.sopsupport.processingrules.rules.*;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;


public class ConditionFactory {


    public static Optional<Condition> create(SoPPair soPPair, ConditionDto conditionDto, RuleConfigurationRepository ruleConfigurationRepository) {


        Function<String, Optional<ProcessingRule>> createRule = c -> {
            Optional<ConditionConfiguration> conditionConfiguration = ruleConfigurationRepository.getConditionConfigurationFor(c);

            Optional<ProcessingRule> processingRuleOptional =
                    !conditionConfiguration.isPresent() ?
                            BuildRuleFromCode(c) :
                            Optional.ofNullable(BuildRuleFromConfig(conditionConfiguration.get()));

            return processingRuleOptional;
        };

        Optional<ProcessingRule> processingRuleOptional = createRule.apply(conditionDto.get_conditionName());
        if (!processingRuleOptional.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new OnsetCondition(
                soPPair,
                conditionDto.get_incidentDateRangeDto().get_startDate(),
                conditionDto.get_incidentDateRangeDto().get_endDate(),
                processingRuleOptional.get())
        );
    }

    private static Optional<ProcessingRule> BuildRuleFromCode(String conditionName) {
        switch (conditionName) {
            case "sprain and strain":
                return Optional.of(new AcuteConditionRule(
                        "F2011L01726", ImmutableSet.of("6(a)", "6(c)"),
                        "F2011L01727", ImmutableSet.of("6(a)", "6(c)"),
                        condition -> new Interval(condition.getStartDate().minusDays(7),condition.getStartDate())));
            default:
                return Optional.empty();
        }
    }


    private static ProcessingRule BuildRuleFromConfig(ConditionConfiguration conditionConfiguration) {
        switch (conditionConfiguration.getConditionName()) {
            case "lumbar spondylosis":
                return new LumbarSpondylosisRule(conditionConfiguration);
            case "osteoarthritis":
                return new OsteoarthritisRule(conditionConfiguration);
            case "intervertebral disc prolapse":
                return new InvertebralDiscProlapseRule(conditionConfiguration);
            case "thoracic spondylosis":
                return new ThoracicSpondylosisRule(conditionConfiguration);
            case "rotator cuff syndrome":
                return new RotatorCuffSyndromeRule(conditionConfiguration);
            case "acquired cataract":
                return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "Achilles tendonopathy and bursitis":
                return new GenericProcessingRule(conditionConfiguration, new FixedDaysPeriodSelector(28));
            case "chondromalacia patella":
                return new GenericProcessingRule(conditionConfiguration, new FixedDaysPeriodSelector(28));
            case "iliotibial band syndrome":
                return new GenericProcessingRule(conditionConfiguration, new FixedDaysPeriodSelector(28));
            case "malignant melanoma of the skin":
                return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "non-melanotic malignant neoplasm of the skin":
                return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "patellar tendinopathy":
                return new GenericProcessingRule(conditionConfiguration, new FixedDaysPeriodSelector(28));
            case "plantar fasciitis":
                return new GenericProcessingRule(conditionConfiguration, new FixedDaysPeriodSelector(365));
            case "pterygium":
                return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "sensorineural hearing loss":
                return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "shin splints":
                return new GenericProcessingRule(conditionConfiguration, new FixedDaysPeriodSelector(28));
            case "solar keratosis":
                return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "tinea":
                return new GenericProcessingRule(conditionConfiguration, new FixedDaysPeriodSelector(28));
            case "tinnitus":
                return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "trochanteric bursitis and gluteal tendinopathy":
                return new GenericProcessingRule(conditionConfiguration, new FixedDaysPeriodSelector(28));
            case "femoroacetabular impingement syndrome":
                return new GenericProcessingRule(conditionConfiguration, new FixedDaysPeriodSelector(28));
        }

        return null;
    }


    public static Optional<SoPPair> getSopPairForConditionName(ImmutableSet<SoPPair> sopPairs, String conditionName) {
        Optional<SoPPair> soPPair = sopPairs.stream().filter(s -> s.getConditionName().equalsIgnoreCase(conditionName.trim()))
                .findFirst();
        return soPPair;
    }


}
