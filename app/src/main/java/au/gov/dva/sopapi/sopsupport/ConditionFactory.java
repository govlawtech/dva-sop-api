package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.exceptions.ProcessingRuleRuntimeException;
import au.gov.dva.sopapi.interfaces.ConditionConfiguration;
import au.gov.dva.sopapi.interfaces.ProcessingRule;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.AllDaysOfServiceSelector;
import au.gov.dva.sopapi.sopsupport.processingrules.intervalSelectors.FixedDaysPeriodSelector;
import au.gov.dva.sopapi.sopsupport.processingrules.rules.*;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.function.Predicate;


public class ConditionFactory {


    public static Optional<Condition> create(ImmutableSet<SoPPair> sopPairs, ConditionDto conditionDto, RuleConfigurationRepository ruleConfigurationRepository, Predicate<Deployment> isOperational) {

        Optional<SoPPair> soPPairOptional = getSopPairForConditionName(sopPairs, conditionDto.get_conditionName());
        if (!soPPairOptional.isPresent()) {
            return Optional.empty();
        }

        Optional<ConditionConfiguration> conditionConfiguration = ruleConfigurationRepository.getConditionConfigurationFor(conditionDto.get_conditionName());
        if (!conditionConfiguration.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(new OnsetCondition(
                soPPairOptional.get(),
                conditionDto.get_incidentDateRangeDto().get_startDate(),
                conditionDto.get_incidentDateRangeDto().get_endDate(),
                BuildRule(conditionConfiguration.get())));
    }


    private static ProcessingRule BuildRule(ConditionConfiguration conditionConfiguration)
    {
        switch (conditionConfiguration.getConditionName())
        {
            case "lumbar spondylosis": return new LumbarSpondylosisRule(conditionConfiguration);
            case "osteoarthritis": return new OsteoarthritisRule(conditionConfiguration);
            case "intervertebral disc prolapse": return new InvertebralDiscProlapseRule(conditionConfiguration);
            case "thoracic spondylosis": return new ThoracicSpondylosisRule(conditionConfiguration);
            case "rotator cuff syndrome": return new RotatorCuffSyndromeRule(conditionConfiguration);
            case "acquired cataract": return new GenericProcessingRule(conditionConfiguration,new AllDaysOfServiceSelector());
            case "Achilles tendonopathy and bursitis" : return new GenericProcessingRule(conditionConfiguration,new FixedDaysPeriodSelector(28));
            case "chondromalacia patella" : return new GenericProcessingRule(conditionConfiguration,new FixedDaysPeriodSelector(28));
            case "iliotibial band syndrome" : return new GenericProcessingRule(conditionConfiguration,new FixedDaysPeriodSelector(28));
            case "malignant melanoma of the skin": return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "non-melanotic malignant neoplasm of the skin" :return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "patellar tendinopathy": return new GenericProcessingRule(conditionConfiguration,new FixedDaysPeriodSelector(28));
            case "plantar fasciitis": return new GenericProcessingRule(conditionConfiguration,new FixedDaysPeriodSelector(365));
            case "pterygium": return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "sensorineural hearing loss": return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "shin splints": return new GenericProcessingRule(conditionConfiguration,new FixedDaysPeriodSelector(28));
            case "solar keratosis": return new GenericProcessingRule(conditionConfiguration,new AllDaysOfServiceSelector());
            case "tinea": return new GenericProcessingRule(conditionConfiguration, new FixedDaysPeriodSelector(28));
            case "tinnitus": return new GenericProcessingRule(conditionConfiguration, new AllDaysOfServiceSelector());
            case "trochanteric bursitis and gluteal tendinopathy": return new GenericProcessingRule(conditionConfiguration,new FixedDaysPeriodSelector(28));
        }

        throw new ProcessingRuleRuntimeException("No rule implemented for " + conditionConfiguration.getConditionName());
    }


    private static Optional<SoPPair> getSopPairForConditionName(ImmutableSet<SoPPair> sopPairs, String conditionName) {
        Optional<SoPPair> soPPair = sopPairs.stream().filter(s -> s.getConditionName().equalsIgnoreCase(conditionName.trim()))
                .findFirst();
        return soPPair;
    }


}
