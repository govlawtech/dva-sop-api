package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopsupport.processingrules.Interval;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.AllDaysOfServiceSelector;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.FixedDaysPeriodSelector;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.FixedYearsPeriodSelector;
import au.gov.dva.sopapi.sopsupport.processingrules.rules.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.stream.Collectors;

public class ConditionFactory {

    public static ImmutableList<String> getAcuteConditions() {
        return acuteConditionsMap.keySet().asList().stream().sorted().collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }

    private static ImmutableMap<String, AcuteProcessingRule> acuteConditionsMap = buildAcuteConditionMap();

    private static ImmutableMap<String, AcuteProcessingRule> buildAcuteConditionMap() {

        ImmutableMap<String, AcuteProcessingRule> map = new ImmutableMap.Builder<String,AcuteProcessingRule>()
                .put("sprain and strain", buildAcuteConditionRule(
                        "F2020L00482", ImmutableSet.of("9(1)", "9(3)"),
                        "F2020L00483", ImmutableSet.of("9(1)", "9(3)"),
                        7))
                .put("acute articular cartilage tear", buildAcuteConditionRule(
                        "F2019L00233",
                        ImmutableSet.of("9(1)"),
                        "F2019L00234",
                        ImmutableSet.of("9(1)"),
                        7
                ))
                .put("acute meniscal tear of the knee", buildAcuteConditionRule(
                        "F2019L00246",
                        ImmutableSet.of("9(1)"),
                        "F2019L00247",
                        ImmutableSet.of("9(1)"),
                        7
                ))
                .put("dislocation of a joint and subluxation of a joint", buildAcuteConditionRule(
                        "F2019L00640",
                        ImmutableSet.of("9(1)"),
                        "F2019L00647",
                        ImmutableSet.of("9(1)"),
                        7
                ))
                .put("fracture", buildAcuteConditionRule(
                        "F2015L01340",
                        ImmutableSet.of("9(1)"),
                        "F2015L01343",
                        ImmutableSet.of("9(1)"),
                        7

                ))
                .put("joint instability", buildAcuteConditionRule(
                        "F2019L00645",
                        ImmutableSet.of("9(1)"),
                        "F2019L00644",
                        ImmutableSet.of("9(1)"),
                        7
                ))
                .put("labral tear", buildAcuteConditionRule(
                        "F2017L00885",
                        ImmutableSet.of("9(1)"),
                        "F2017L00886",
                        ImmutableSet.of("9(1)"),
                        7
                ))
                .put("external bruise", buildAcuteConditionRule(
                        "F2016L00008",
                        ImmutableSet.of("9(1)"),
                        "F2016L00005",
                        ImmutableSet.of("9(1)"),
                        7
                ))
                .put("external burn", buildAcuteConditionRule(
                        "F2017C00862",
                        ImmutableSet.of("9(1)"),
                        "F2017C00861",
                        ImmutableSet.of("9(1)"),
                        7
                ))
                .put("gunshot injury", buildAcuteConditionRule(
                        "F2020L00495",
                        ImmutableSet.of("9(1)"),
                        "F2020L00491",
                        ImmutableSet.of("9(1)"),
                        7
                ))
                .put("explosive blast injury", buildAcuteConditionRule(
                        "F2020L00485",
                        ImmutableSet.of("9(1)"),
                        "F2020L00487",
                        ImmutableSet.of("9(1)"),
                        7
                ))
                .put("cut, stab, abrasion and laceration", buildAcuteConditionRule(
                        "F2016L00567",
                        ImmutableSet.of("9(1)"),
                        "F2016L00571",
                        ImmutableSet.of("9(1)"),
                        7
                ))
                .build();

        return map;
    }

    public static Optional<Condition> createAcuteCondition(SoPPair soPPair, ConditionDto conditionDto)
    {
        Optional<AcuteProcessingRule> acuteProcessingRule = BuildAcuteRuleFromCode(conditionDto.get_conditionName());
        if (!acuteProcessingRule.isPresent()) return Optional.empty();
        return Optional.of(new OnsetCondition(
                soPPair,
                conditionDto.get_incidentDateRangeDto().get_startDate(),
                conditionDto.get_incidentDateRangeDto().get_endDate(),
                acuteProcessingRule.get())
        );

    }


    public static Optional<Condition> createWearAndTearCondition(SoPPair soPPair, ConditionDto conditionDto, ApplicableWearAndTearRuleConfiguration applicableWearAndTearRuleConfiguration) {


        WearAndTearProcessingRule wearAndTearProcessingRule = BuildWearAndTearRuleFromConfig(applicableWearAndTearRuleConfiguration); // can be null
        if (wearAndTearProcessingRule == null) return Optional.empty();

        return Optional.of(new OnsetCondition(
                soPPair,
                conditionDto.get_incidentDateRangeDto().get_startDate(),
                conditionDto.get_incidentDateRangeDto().get_endDate(),
                wearAndTearProcessingRule
        ));
    }

    private static AcuteProcessingRule buildAcuteConditionRule(String rhRegisterId, ImmutableSet<String> rhParas, String bopRegisterId, ImmutableSet<String> bopParas, int daysWindowBeforeOnset) {
        return new AcuteConditionRule(rhRegisterId, rhParas, bopRegisterId, bopParas,
                condition -> new Interval(condition.getStartDate().minusDays(daysWindowBeforeOnset), condition.getStartDate()));
    }

    private static Optional<AcuteProcessingRule> BuildAcuteRuleFromCode(String conditionName) {

        if (acuteConditionsMap.containsKey(conditionName)) {
            return Optional.of(acuteConditionsMap.get(conditionName));
        }

        return Optional.empty();
    }


    private static WearAndTearProcessingRule BuildWearAndTearRuleFromConfig(ApplicableWearAndTearRuleConfiguration applicableWearAndTearRuleConfiguration) {
        switch (applicableWearAndTearRuleConfiguration.getConditionName()) {
            case "lumbar spondylosis":
                return new LumbarSpondylosisRule(applicableWearAndTearRuleConfiguration);
            case "osteoarthritis":
                return new OsteoarthritisRule(applicableWearAndTearRuleConfiguration);
            case "intervertebral disc prolapse":
                return new InvertebralDiscProlapseRule(applicableWearAndTearRuleConfiguration);
            case "thoracic spondylosis":
                return new ThoracicSpondylosisRule(applicableWearAndTearRuleConfiguration);
            case "rotator cuff syndrome":
                return new RotatorCuffSyndromeRule(applicableWearAndTearRuleConfiguration);
            case "acquired cataract":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());
            case "Achilles tendinopathy and bursitis":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new FixedDaysPeriodSelector(28));
            case "chondromalacia patella":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new FixedDaysPeriodSelector(28));
            case "iliotibial band syndrome":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new FixedDaysPeriodSelector(28));
            case "malignant melanoma of the skin":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());
            case "non-melanotic malignant neoplasm of the skin":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());
            case "patellar tendinopathy":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new FixedDaysPeriodSelector(28));
            case "plantar fasciitis":
                return new GenericProcessingRuleWithUniqueTestPeriodsForRHandBoP(applicableWearAndTearRuleConfiguration,
                        new FixedDaysPeriodSelector(84), // test period for standard of proof
                        new FixedDaysPeriodSelector(84), // RH
                        new FixedDaysPeriodSelector(168)); // BoP
            case "pterygium":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());
            case "sensorineural hearing loss":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());
            case "shin splints":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new FixedDaysPeriodSelector(28));
            case "solar keratosis":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());
            case "tinea":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new FixedDaysPeriodSelector(28));
            case "tinnitus":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());
            case "trochanteric bursitis and gluteal tendinopathy":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new FixedDaysPeriodSelector(28));
            case "femoroacetabular impingement syndrome":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new FixedDaysPeriodSelector(28));
            case "posttraumatic stress disorder":
                return new MentalHealthProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());
            case "anxiety disorder":
                return new MentalHealthProcessingRule(applicableWearAndTearRuleConfiguration, new FixedYearsPeriodSelector(5));
            case "adjustment disorder":
                return new MentalHealthProcessingRule(applicableWearAndTearRuleConfiguration, new FixedDaysPeriodSelector(84));
            case "malignant neoplasm of the eye":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());
            case "seborrhoeic keratosis":
                return new RhOnlyGenericProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());
            case "pinguecula":
                return new GenericWearAndTearProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());
            case "benign neoplasm of the eye and adnexa":
                return new RhOnlyGenericProcessingRule(applicableWearAndTearRuleConfiguration, new AllDaysOfServiceSelector());

        }

        return null;
    }


    public static Optional<SoPPair> getSopPairForConditionName(ImmutableSet<SoPPair> sopPairs, String conditionName) {
        Optional<SoPPair> soPPair = sopPairs.stream().filter(s -> s.getConditionName().equalsIgnoreCase(conditionName.trim()))
                .findFirst();
        return soPPair;
    }


}
