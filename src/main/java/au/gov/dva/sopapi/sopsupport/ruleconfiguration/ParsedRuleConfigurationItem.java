package au.gov.dva.sopapi.sopsupport.ruleconfiguration;

import au.gov.dva.sopapi.ConfigurationError;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.exceptions.SopParserError;
import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.RuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.model.InstrumentChange;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ParsedRuleConfigurationItem implements RuleConfigurationItem {
    @Override
    public String toString() {
        return "ParsedRuleConfigurationItem{" +
                "conditionName='" + conditionName + '\'' +
                ", instrumentId='" + instrumentId + '\'' +
                ", factorRefs=" + factorRefs +
                ", serviceBranch=" + serviceBranch +
                ", rank=" + rank +
                ", cftsWeeks=" + cftsWeeks +
                ", accumRate=" + accumRate +
                ", accumUnit=" + accumUnit +
                '}';
    }

    private String conditionName;
    private String instrumentId;
    private ImmutableSet<String> factorRefs;
    private final ServiceBranch serviceBranch;
    private final Rank rank;
    private final int cftsWeeks;
    private final Optional<Integer> accumRate;
    private final Optional<String> accumUnit;

    public ParsedRuleConfigurationItem(@Nonnull String conditionName, @Nonnull String instrumentId, @Nonnull String factorRefs,@Nonnull String serviceBranch,@Nonnull String rank, String cftsWeeks, @Nonnull Optional<String> accumRate, @Nonnull Optional<String> accumUnit)
    {
        this.conditionName = conditionName.trim().toLowerCase(Locale.US);
        this.instrumentId = instrumentId.trim();
        this.factorRefs = splitFactorRefs(factorRefs);
        this.rank = toRank(rank);
        this.serviceBranch = toServiceBranch(serviceBranch);
        this.cftsWeeks = toInt(cftsWeeks,"Cannot determine number of CFTS weeks from");
        this.accumRate = accumRate.isPresent() ? Optional.of(toInt(accumRate.get(),"Cannot determine the accumulation rate from")) : Optional.empty();
        this.accumUnit = accumUnit;
    }

    private ImmutableSet<String> splitFactorRefs(String factorRefsCellValue){

        List<String> refs =  Arrays.stream(
                factorRefsCellValue.split("[,;]"))
                .map(String::trim)
                .collect(Collectors.toList());


        refs.forEach(r ->  {
            if (!r.matches("[0-9\\(\\)a-z]+"))
            {
                throw new ConfigurationError(String.format("Illegal factor reference in cell: %s", factorRefsCellValue));
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
            throw new ConfigurationError(e);
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
            throw new ConfigurationError(e);
        }
    }

    protected int toInt(String intString, String errMsg)
    {
        try {
            return Integer.parseInt(intString.trim());
        }
        catch (Exception e)
        {
            throw new ConfigurationError(errMsg + ": " + intString);
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
    public int getRequiredCFTSWeeks() {
        return cftsWeeks;
    }

    @Override
    public Optional<Integer> getAccumulationRatePerWeek() {
        return accumRate;
    }

    @Override
    public Optional<String> getAccumulationUnit() {
        return accumUnit;
    }

}

