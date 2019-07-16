package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.LumbarSpondylosisConditionMockWithOnsetDate;
import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
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


    // start 1 Jan 2005
    // one year in Army
    ServiceHistory CreateMockServiceHistoryForArmyToAirForce(int cftsArmyDays, int operationalArmyDays, int cftsAirforceDays, int operationalAirforceDays)
    {
        return new ServiceHistory() {
            @Override
            public LocalDate getHireDate() {
                return LocalDate.of(2004,7,1);
            }

            @Override
            public ImmutableSet<Service> getServices() {
                Service armyService = new Service() {
                    @Override
                    public ServiceBranch getBranch() {
                        return ServiceBranch.ARMY;
                    }

                    @Override
                    public EmploymentType getEmploymentType() {
                        return EmploymentType.CFTS;
                    }

                    @Override
                    public Rank getRank() {
                        return Rank.OtherRank;
                    }

                    @Override
                    public ImmutableSet<Deployment> getDeployments() {
                        return ImmutableSet.of(
                                new Deployment() {
                                    @Override
                                    public String getOperationName() {
                                        return "SLIPPER";
                                    }

                                    @Override
                                    public String getEvent() {
                                        return "Within specified area";
                                    }

                                    @Override
                                    public LocalDate getStartDate() {
                                        return getHireDate();
                                    }

                                    @Override
                                    public Optional<LocalDate> getEndDate() {
                                        return Optional.of(getHireDate().plusDays(operationalArmyDays));
                                    }
                                }
                        );
                    }

                    @Override
                    public LocalDate getStartDate() {
                        return getHireDate();
                    }

                    @Override
                    public Optional<LocalDate> getEndDate() {
                        return Optional.of(getHireDate().plusDays(cftsArmyDays));
                    }
                };

                Service airForceService = new Service() {
                    @Override
                    public ServiceBranch getBranch() {
                        return ServiceBranch.RAAF;
                    }

                    @Override
                    public EmploymentType getEmploymentType() {
                        return EmploymentType.CFTS;
                    }

                    @Override
                    public Rank getRank() {
                        return Rank.OtherRank;
                    }

                    @Override
                    public ImmutableSet<Deployment> getDeployments() {
                        return ImmutableSet.of(
                                new Deployment() {
                                    @Override
                                    public String getOperationName() {
                                        return "SLIPPER";
                                    }

                                    @Override
                                    public String getEvent() {
                                        return "Within specified area";
                                    }

                                    @Override
                                    public LocalDate getStartDate() {
                                        return getHireDate().plusDays(cftsArmyDays);
                                    }

                                    @Override
                                    public Optional<LocalDate> getEndDate() {
                                        return Optional.of(getHireDate().plusDays(cftsArmyDays).plusDays(operationalAirforceDays));
                                    }
                                }
                        );
                    }

                    @Override
                    public LocalDate getStartDate() {
                        return getHireDate().plusDays(cftsArmyDays);
                    }

                    @Override
                    public Optional<LocalDate> getEndDate() {
                        return Optional.of(getHireDate().plusDays(cftsArmyDays).plusDays(cftsAirforceDays));
                    }
                };

                return ImmutableSet.of(armyService,airForceService);

            }

            @Override
            public ServiceHistory filterServiceHistoryByEvents(List<String> eventList) {
                return null;
            }
        };
    }



    @Test
    public void ShouldReturnMultipleApplicableRuleConfigurations()
    {
        ConditionConfiguration mockConditionConfig = CreateMockConfigForArmyToAirForce("lumbar spondylosis"); // has to match condition mock name
        Condition mockCondition = new LumbarSpondylosisConditionMockWithOnsetDate(LocalDate.of(2010,1,1));
        ServiceHistory mockServiceHistory = CreateMockServiceHistoryForArmyToAirForce(150,17,150,17);
        SopSupportCaseTrace mockCaseTrace = new SopSupportCaseTrace();
        ImmutableSet<ApplicableWearAndTearRuleConfiguration> results = mockConditionConfig.getApplicableRuleConfigurations(mockCondition.getSopPair().getConditionName(),mockCondition.getStartDate(),mockServiceHistory,mockCaseTrace);
        Assert.assertTrue(results.size() == 2);
    }

    @Test
    public void ShouldReturnNoMatchingConfigAsNoServicesBeforeOnsetDate()
    {
        ConditionConfiguration mockConditionConfig = CreateMockConfigForArmyToAirForce("lumbar spondylosis"); // has to match condition mock name
        Condition mockCondition = new LumbarSpondylosisConditionMockWithOnsetDate(LocalDate.of(2001,1,1));
        ServiceHistory mockServiceHistory = CreateMockServiceHistoryForArmyToAirForce(150,17,150,17);
        SopSupportCaseTrace mockCaseTrace = new SopSupportCaseTrace();
        ImmutableSet<ApplicableWearAndTearRuleConfiguration> results = mockConditionConfig.getApplicableRuleConfigurations(mockCondition.getSopPair().getConditionName(),mockCondition.getStartDate(),mockServiceHistory,mockCaseTrace);
        Assert.assertTrue(results.size() == 0);
    }




}
