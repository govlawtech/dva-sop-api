package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;
;
import au.gov.dva.sopapi.interfaces.ConditionConfiguration;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.Optional;

public class MultipleBranchesOfServiceTests {



    class MockConfigPair {

        private final RHRuleConfigurationItem rh;
        private final BoPRuleConfigurationItem bop;

        public MockConfigPair(RHRuleConfigurationItem rh, BoPRuleConfigurationItem bop)
        {

            this.rh = rh;
            this.bop = bop;
        }

        public RHRuleConfigurationItem getRH() {
            return rh;
        }

        public BoPRuleConfigurationItem getBoP() {
                return bop;
        }
    }

    // RH threshold is seedDays / 10
    // RH CFTS is seedDays
    // BoP CFTS is seedDays * 2
    MockConfigPair CreateMockConditionConfig(String conditionName, ServiceBranch serviceBranch, Rank rank, int seedDays)
    {
        RHRuleConfigurationItem rh = new RHRuleConfigurationItem() {
            @Override
            public int getRequiredDaysOfOperationalService() {
                return seedDays/10;
            }

            @Override
            public Optional<Integer> getYearsLimitForOperationalService() {
                return Optional.empty();
            }

            @Override
            public String getConditionName() {
                return conditionName;
            }

            @Override
            public String getInstrumentId() {
                return null;
            }

            @Override
            public ImmutableSet<String> getFactorReferences() {
                return null;
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
                return seedDays;
            }
        };

        BoPRuleConfigurationItem bop = new BoPRuleConfigurationItem() {
            @Override
            public String getConditionName() {
                return conditionName;
            }

            @Override
            public String getInstrumentId() {
                return null;
            }

            @Override
            public ImmutableSet<String> getFactorReferences() {
                return null;
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
                return seedDays * 2;
            }
        };

        return new MockConfigPair(rh,bop);
    }

    // Represents config where Air Force has higher day requirements than Army
    ConditionConfiguration CreateMockConfigForArmyToAirForce(String conditionName) {
        MockConfigPair armyConfig = CreateMockConditionConfig(conditionName,ServiceBranch.ARMY,Rank.OtherRank,100);
        MockConfigPair airForceConfig = CreateMockConditionConfig(conditionName,ServiceBranch.RAAF,Rank.OtherRank,200);
        return new ConditionConfiguration() {
            @Override
            public String getConditionName() {
                return conditionName;
            }

            @Override
            public ImmutableSet<RHRuleConfigurationItem> getRHRuleConfigurationItems() {
                return ImmutableSet.of(armyConfig.rh,airForceConfig.rh);
            }

            @Override
            public ImmutableSet<BoPRuleConfigurationItem> getBoPRuleConfigurationItems() {
                return ImmutableSet.of(airForceConfig.bop,armyConfig.bop);
            }
        };
    }

    @Test
    public void ShouldReturnSingleApplicableRuleConfiguration()
    {

    }

    @Test
    public void ShouldReturnMultipleApplicableRuleConfigurations()
    {

    }

}
