package au.gov.dva.sopapi.sopsupport.processingrules;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleRuleSpec implements RuleSpecification {

    private final ImmutableSet<ServiceBranchSpecification> _serviceBranchSpecifications;

    public SimpleRuleSpec(ServiceBranchSpecification ... serviceBranchSpecifications)
    {
        _serviceBranchSpecifications = ImmutableSet.copyOf(Arrays.stream(serviceBranchSpecifications).collect(Collectors.toList()));
    }


    @Override
    public Optional<RankSpecification> getSpec(Rank rank, ServiceBranch serviceBranch)
    {
        Optional<ServiceBranchSpecification> serviceBranchSpecification = _serviceBranchSpecifications.stream()
                .filter(i -> i.getServiceBranch() == serviceBranch)
                .filter(i -> i.getRankSpecifications().stream().anyMatch(rankSpecification -> rankSpecification.getRank() == rank))
                .findFirst();
        if (!serviceBranchSpecification.isPresent())
            return Optional.empty();

        Optional<RankSpecification> rankSpec = serviceBranchSpecification.get()
                .getRankSpecifications()
                .stream()
                .filter(rs -> rs.getRank() == rank)
                .findFirst();

        return rankSpec;

    }


}




