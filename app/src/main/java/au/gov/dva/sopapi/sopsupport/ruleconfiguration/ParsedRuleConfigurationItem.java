package au.gov.dva.sopapi.sopsupport.ruleconfiguration;

import au.gov.dva.sopapi.ConfigurationRuntimeException;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.RuleConfigurationItem;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ParsedRuleConfigurationItem implements RuleConfigurationItem {
    @Override
    public String toString() {
        return "ParsedRuleConfigurationItem{" +
                "conditionName='" + conditionName + '\'' +
                ", instrumentId='" + instrumentId + '\'' +
                ", factorRefs=" + factorRefs +
                ", serviceBranch=" + serviceBranch +
                ", rank=" + rank +
                ", cftsDays=" + cftsDays +
                ", accumRate=" + accumRate +
                ", accumUnit=" + accumUnit +
                ", onsetWindow=" + hardOnsetWindow +
                '}';
    }

    private String conditionName;
    private String instrumentId;
    private ImmutableSet<String> factorRefs;
    private final ServiceBranch serviceBranch;
    private final Rank rank;
    private final int cftsDays;
    private final Optional<Integer> accumRate;
    private final Optional<String> accumUnit;
    private final Optional<String> hardOnsetWindow;

    public ParsedRuleConfigurationItem(@Nonnull String conditionName, @Nonnull String instrumentId, @Nonnull String factorRefs,@Nonnull String serviceBranch,@Nonnull String rank, @Nonnull String cftsDays, @Nonnull Optional<String> accumRate, @Nonnull Optional<String> accumUnit, @Nonnull Optional<String> hardOnsetWindow)
    {
        this.conditionName = conditionName.trim().toLowerCase(Locale.US);
        this.instrumentId = instrumentId.trim();
        this.factorRefs = splitFactorRefs(factorRefs);
        this.rank = toRank(rank);
        this.serviceBranch = toServiceBranch(serviceBranch);
        this.cftsDays = toIntOrError(cftsDays,"Cannot determine number of CFTS days from");
        this.accumRate = accumRate.isPresent() ? Optional.of(toIntOrError(accumRate.get(),"Cannot determine the accumulation rate from")) : Optional.empty();
        this.accumUnit = accumUnit;
        this.hardOnsetWindow = hardOnsetWindow;
    }



    private ImmutableSet<String> splitFactorRefs(String factorRefsCellValue){

        List<String> refs =  Arrays.stream(
                factorRefsCellValue.split("[,;]"))
                .map(String::trim)
                .collect(Collectors.toList());


        refs.forEach(r ->  {
            if (!Pattern.matches(CsvRuleConfigurationRepository.regexForFactorRef,r))
            {
                throw new ConfigurationRuntimeException(String.format("Illegal factor reference in cell: %s", factorRefsCellValue));
            }
        });

        return ImmutableSet.copyOf(refs);
    }

    private Rank toRank(String rank)
    {
        try {
            Rank parsed = Rank.fromString(rank.trim());
            return parsed;
        }
        catch (Exception e)
        {
            throw new ConfigurationRuntimeException(e);
        }
    }

    private ServiceBranch toServiceBranch(String serviceBranch)
    {
        try {
            ServiceBranch parsed = ServiceBranch.fromString(serviceBranch.trim());
            return parsed;
        }
        catch (Exception e)
        {
            throw new ConfigurationRuntimeException(e);
        }
    }

    protected int toIntOrError(String intString, String errMsg)
    {
        try {
            return Integer.parseInt(intString.trim());
        }
        catch (Exception e)
        {
            throw new ConfigurationRuntimeException(errMsg + ": " + intString);
        }
    }

    @Override
    public String getConditionName() {
        return conditionName;
    }

    @Override
    public String getInstrumentId() {
        return instrumentId;
    }

    @Override
    public ImmutableSet<String> getFactorReferences() {
        return factorRefs;
    }

    @Override
    public ServiceBranch getServiceBranch() {
        return serviceBranch;
    }

    @Override
    public Rank getRank() {
        return rank;
    }

    @Override
    public int getRequiredCFTSDays() {
        return cftsDays;
    }

    @Override
    public Optional<Integer> getAccumulationRatePerWeek() {
        return accumRate;
    }

    @Override
    public Optional<String> getAccumulationUnit() {
        return accumUnit;
    }

    @Override
    public Optional<String> getHardOnsetWindow() {
        return hardOnsetWindow;
    }
}

