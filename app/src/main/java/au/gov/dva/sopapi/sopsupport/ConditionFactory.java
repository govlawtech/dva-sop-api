package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.interfaces.ConditionConfiguration;
import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.AllDaysOfServiceSelector;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.FixedDaysPeriodSelector;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.FixedYearsPeriodSelector;
import au.gov.dva.sopapi.sopsupport.processingrules.rules.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Function;


public class ConditionFactory {


    public static ImmutableList<String> getAcuteConditions() {
        return ImmutableList.of(
                "sprain and strain",
                "acute articular cartilage tear",
                "acute meniscal tear of the knee",
                "dislocation",
                "fracture",
                "joint instability",
                "labral tear",
                "external bruise",
                "external burn"
        );
    }

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

    private static ProcessingRule buildAcuteConditionRule(String rhRegisterId, ImmutableSet<String> rhParas, String bopRegisterId, ImmutableSet<String> bopParas, int daysWindowBeforeOnset)
    {
        return new AcuteConditionRule(rhRegisterId,rhParas,bopRegisterId,bopParas,
                condition -> new Interval(condition.getStartDate().minusDays(daysWindowBeforeOnset), condition.getStartDate()));


    }

    private static Optional<ProcessingRule> BuildRuleFromCode(String conditionName) {
        switch (conditionName) {
            case "sprain and strain":
                return Optional.of(new AcuteConditionRule(
                        "F2011L01726", ImmutableSet.of("6(a)", "6(c)"),
                        "F2011L01727", ImmutableSet.of("6(a)", "6(c)"),
                        condition -> new Interval(condition.getStartDate().minusDays(7), condition.getStartDate())));
            case "acute articular cartilage tear":
                return Optional.of(buildAcuteConditionRule(
                        "F2010L01666",
                        ImmutableSet.of("6(a)"),
                        "F2010L01667",
                        ImmutableSet.of("6(a)"),
                        7
                ));

            case "acute meniscal tear of the knee":
                return Optional.of(buildAcuteConditionRule(
                        "F2010L01668",
                        ImmutableSet.of("6(a)"),
                        "F2010L01669",
                        ImmutableSet.of("6(a)"),
                        7
                ));

            case "dislocation":
                return Optional.of(buildAcuteConditionRule(
                        "F2010L01040",
                        ImmutableSet.of("6(a)"),
                        "F2010L01041",
                        ImmutableSet.of("6(a)"),
                        7
                ));

            case "fracture":
                return Optional.of(buildAcuteConditionRule(
                        "F2015L01340",
                        ImmutableSet.of("9(1)"),
                        "F2015L01343",
                        ImmutableSet.of("9(1)"),
                        7

                ));

            case "joint instability":
                return Optional.of(buildAcuteConditionRule(
                        "F2010L01048",
                        ImmutableSet.of("6(a)"),
                        "F2010L01049",
                        ImmutableSet.of("6(a)"),
                        7
                ));

            case "labral tear":
                return Optional.of(buildAcuteConditionRule(
                       "F2017L00885",
                       ImmutableSet.of("9(1)"),
                        "F2017L00886",
                        ImmutableSet.of("9(1)"),
                        7
                ));

            case "external bruise":
                return Optional.of(buildAcuteConditionRule(
                        "F2016L00008",
                        ImmutableSet.of("9(1)"),
                        "F2016L00005",
                        ImmutableSet.of("9(1)"),
                        7
                ));

            case "external burn":
                return Optional.of(buildAcuteConditionRule(
                        "F2017C00862",
                        ImmutableSet.of("9(1)"),
                        "F2017C00861",
                        ImmutableSet.of("9(1)"),
                        7
                ));

             case "concerning physical injury due to munitions discharge":
                return Optional.of(buildAcuteConditionRule(
                        "F2012L01789",
                        ImmutableSet.of("6(a)"),
                        "F2012L01790",
                        ImmutableSet.of("6(a)"),
                        7
                ));

                 case "cut, stab, abrasion and laceration":
                return Optional.of(buildAcuteConditionRule(
                        "F2016L00567",
                        ImmutableSet.of("9(1)"),
                        "F2016L00571",
                        ImmutableSet.of("9(1)"),
                        7
                ));


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
            case "achilles tendinopathy and bursitis":
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
                return new GenericProcessingRuleWithUniqueTestPeriodsForRHandBoP(conditionConfiguration,
                        new FixedDaysPeriodSelector(84), // test period for standard of proof
                        new FixedDaysPeriodSelector(84), // RH
                        new FixedDaysPeriodSelector(168)); // BoP
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
            case "posttraumatic stress disorder":
                return new MentalHealthProcessingRule(conditionConfiguration,new AllDaysOfServiceSelector());
            case "anxiety disorder":
                return new MentalHealthProcessingRule(conditionConfiguration,new FixedYearsPeriodSelector(5));
            case "adjustment disorder":
                return new MentalHealthProcessingRule(conditionConfiguration,new FixedDaysPeriodSelector(84));
            case "malignant neoplasm of the eye":
                return new GenericProcessingRule(conditionConfiguration,new AllDaysOfServiceSelector());
            case "seborrhoeic keratosis":
                return new RhOnlyGenericProcessingRule(conditionConfiguration,new AllDaysOfServiceSelector());
            case "pinguecula":
                return new GenericProcessingRule(conditionConfiguration,new AllDaysOfServiceSelector());
            case "benign neoplasm of the eye and adnexa":
                return new RhOnlyGenericProcessingRule(conditionConfiguration,new AllDaysOfServiceSelector());

        }

        return null;
    }


    public static Optional<SoPPair> getSopPairForConditionName(ImmutableSet<SoPPair> sopPairs, String conditionName) {
        Optional<SoPPair> soPPair = sopPairs.stream().filter(s -> s.getConditionName().equalsIgnoreCase(conditionName.trim()))
                .findFirst();
        return soPPair;
    }


}
