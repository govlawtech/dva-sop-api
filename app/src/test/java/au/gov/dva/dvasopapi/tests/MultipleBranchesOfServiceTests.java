package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.ConditionMock;
import au.gov.dva.dvasopapi.tests.mocks.LumbarSpondylosisConditionMock;
import au.gov.dva.sopapi.dtos.EmploymentType;
import au.gov.dva.sopapi.dtos.Rank;
import au.gov.dva.sopapi.dtos.ServiceBranch;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Deployment;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import au.gov.dva.sopapi.sopsupport.processingrules.rules.ProcessingRuleBase;
import com.google.common.collect.ImmutableSet;
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
                return LocalDate.of(2005,1,1);
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

    class ProcessingRuleBaseUnderTest extends ProcessingRuleBase {

        public ProcessingRuleBaseUnderTest(ConditionConfiguration conditionConfiguration) {
            super(conditionConfiguration);
        }

        public ImmutableSet<ApplicableRuleConfiguration> methodToTest(ServiceHistory serviceHistory, Condition condition, CaseTrace caseTrace)
        {
            return this.getApplicableRuleConfigurations(serviceHistory,condition,caseTrace);
        }
    }

    @Test
    public void ShouldReturnMultipleApplicableRuleConfigurations()
    {
        ConditionConfiguration mockConditionConfig = CreateMockConfigForArmyToAirForce("lumbar spondylosis"); // has to match condition mock name
        Condition mockCondition = new LumbarSpondylosisConditionMock();
        ProcessingRuleBaseUnderTest underTest = new ProcessingRuleBaseUnderTest(mockConditionConfig);
        CaseTrace mockCaseTrace = new SopSupportCaseTrace("mock case ID");
        // enough to meet reqs for Army, but not AirForce
        ServiceHistory mockServiceHistory = CreateMockServiceHistoryForArmyToAirForce(150,17,150,17);
        ImmutableSet<ApplicableRuleConfiguration> results = underTest.methodToTest(mockServiceHistory,mockCondition,mockCaseTrace);

    }


}
