package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopsupport.processingrules.RuleConfigRepositoryUtils;
import au.gov.dva.sopapi.sopsupport.processingrules.rules.GenericProcessingRule;
import au.gov.dva.sopapi.sopsupport.processingrules.rules.LumbarSpondylosisRule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.stream.Collectors;

public class ConditionFactory {

    public static Optional<Condition> create(ImmutableSet<SoPPair> sopPairs, ConditionDto conditionDto, RuleConfigurationRepository ruleConfigurationRepository)
    {

        Optional<SoPPair> soPPairOptional = getSopPairForConditionName(sopPairs,conditionDto.get_conditionName());
        if (!soPPairOptional.isPresent())
        {
            return Optional.empty();
        }

        if (conditionDto.get_conditionName().contentEquals("lumbar spondylosis") && conditionDto.get_incidentType() == IncidentType.Onset)
        {
            return Optional.of(new OnsetCondition(
                    soPPairOptional.get() ,
                    conditionDto.get_incidentDateRangeDto().get_startDate(),
                    conditionDto.get_incidentDateRangeDto().get_endDate(),
                    new LumbarSpondylosisRule(ruleConfigurationRepository)));
        }

        else if (RuleConfigRepositoryUtils.containsConfigForCondition(conditionDto.get_conditionName(),ruleConfigurationRepository))
        {
            return Optional.of(new OnsetCondition(
                    soPPairOptional.get() ,
                    conditionDto.get_incidentDateRangeDto().get_startDate(),
                    conditionDto.get_incidentDateRangeDto().get_endDate(),
                    new GenericProcessingRule(ruleConfigurationRepository)));
        }

        else {
            return Optional.empty();
        }

    }


    private static Optional<SoPPair>  getSopPairForConditionName(ImmutableSet<SoPPair> sopPairs, String conditionName)
    {

        Optional<SoPPair> soPPair =  sopPairs.stream().filter(s -> s.getConditionName().equalsIgnoreCase(conditionName.trim()))
               .findFirst();
        return soPPair;
    }



}
