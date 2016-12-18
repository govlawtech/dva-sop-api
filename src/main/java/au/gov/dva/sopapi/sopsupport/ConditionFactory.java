package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.sopsupport.components.ConditionDto;
import au.gov.dva.sopapi.exceptions.ProcessingRuleError;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.ServiceDetermination;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import au.gov.dva.sopapi.sopsupport.processingrules.LumbarSpondylosisRule;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;

// match rules to conditions by name
// determine whether aggravated or not
public class ConditionFactory {

    Condition create(ImmutableSet<SoPPair> sops, ConditionDto conditionDto, ServiceDetermination warlikeDetermination, ServiceDetermination nonWarlikeDetermination)
    {

        // todo: generic rule for acute conditions

        // match to rule based
        if (conditionDto.get_conditionName().contentEquals("lumbar spondylosis") && conditionDto.get_incidentType() == IncidentType.Onset)
        {

            // todo: check instrument ID's
            return new OnsetCondition(
                    getSopPairForConditionName(sops,conditionDto.get_conditionName()),
                    conditionDto.get_incidentDateRangeDto().get_startDate(),
                    conditionDto.get_incidentDateRangeDto().get_endDate(),
                    new LumbarSpondylosisRule());
        }
        throw new ProcessingRuleError(String.format("No processing rule defined for condition: %s.", conditionDto.get_conditionName()));
    }



    private static SoPPair  getSopPairForConditionName(ImmutableSet<SoPPair> sops, String conditionName)
    {
        Optional<SoPPair> soPPair =  sops.stream().filter(s -> s.getConditionName().contentEquals(conditionName))
                .findFirst();
        if (!soPPair.isPresent())
        {
            throw new ProcessingRuleError(String.format("No processing rule defined for condition: %s.", conditionName));
        }
        return soPPair.get();
    }

}
