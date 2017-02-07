package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.exceptions.ProcessingRuleError;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopsupport.processingrules.LumbarSpondylosisRule;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;

// match rules to conditions by name
// determine whether aggravated or not
public class ConditionFactory {

    public static Condition create(ImmutableSet<SoPPair> sopPairs, ConditionDto conditionDto)
    {

        // todo: generic rule for acute conditions

        // match to rule based
        if (conditionDto.get_conditionName().contentEquals("lumbar spondylosis") && conditionDto.get_incidentType() == IncidentType.Onset)
        {
            // todo: check instrument ID's
            return new OnsetCondition(
                    getSopPairForConditionName(sopPairs,conditionDto.get_conditionName()),
                    conditionDto.get_incidentDateRangeDto().get_startDate(),
                    conditionDto.get_incidentDateRangeDto().get_endDate(),
                    new LumbarSpondylosisRule());
        }
        throw new ProcessingRuleError(String.format("No processing rule defined for condition: '%s' with incident type: '%s'.", conditionDto.get_conditionName(),conditionDto.get_incidentType().toString()));
    }



    private static SoPPair  getSopPairForConditionName(ImmutableSet<SoPPair> sopPairs, String conditionName)
    {
        Optional<SoPPair> soPPair =  sopPairs.stream().filter(s -> s.getConditionName().contentEquals(conditionName))
                .findFirst();
        if (!soPPair.isPresent())
        {
            throw new ProcessingRuleError(String.format("No pair of BoP and RH sops available for condition '%s'.", conditionName));
        }
        return soPPair.get();
    }

}
