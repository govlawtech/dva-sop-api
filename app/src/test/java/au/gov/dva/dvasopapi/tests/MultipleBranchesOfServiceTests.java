package au.gov.dva.dvasopapi.tests;

import au.gov.dva.dvasopapi.tests.mocks.LumbarSpondylosisConditionMockWithOnsetDate;
import au.gov.dva.sopapi.dtos.*;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.*;
import au.gov.dva.sopapi.interfaces.*;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import au.gov.dva.sopapi.sopsupport.processingrules.RulesResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class MultipleBranchesOfServiceTests {


    class MockConfigPair {

        private final RHRuleConfigurationItem rh;
        private final BoPRuleConfigurationItem bop;

        public MockConfigPair(RHRuleConfigurationItem rh, BoPRuleConfigurationItem bop) {
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
    MockConfigPair CreateMockConditionConfig(String conditionName, ServiceBranch serviceBranch, Rank rank, int seedDays) {
        RHRuleConfigurationItem rh = new RHRuleConfigurationItem() {
            @Override
            public int getRequiredDaysOfOperationalService() {
                return seedDays / 10;
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
            public ImmutableSet<FactorReference> getFactorRefObjects() {
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
            public ImmutableSet<FactorReference> getFactorRefObjects() {
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

            @Override
            public ImmutableSet<String> getMainFactorReferences() {
                return null;
            }
        };

        return new MockConfigPair(rh, bop);
    }

    // Represents config where Air Force has higher day requirements than Army
    ConditionConfiguration CreateMockConfigForArmyToAirForce(String conditionName) {
        MockConfigPair armyConfig = CreateMockConditionConfig(conditionName, ServiceBranch.ARMY, Rank.OtherRank, 100);
        MockConfigPair airForceConfig = CreateMockConditionConfig(conditionName, ServiceBranch.RAAF, Rank.OtherRank, 200);
        return new ConditionConfiguration() {
            @Override
            public String getConditionName() {
                return conditionName;
            }

            @Override
            public ImmutableSet<RHRuleConfigurationItem> getRHRuleConfigurationItems() {
                return ImmutableSet.of(armyConfig.rh, airForceConfig.rh);
            }

            @Override
            public ImmutableSet<BoPRuleConfigurationItem> getBoPRuleConfigurationItems() {
                return ImmutableSet.of(airForceConfig.bop, armyConfig.bop);
            }
        };
    }


    // start 1 Jan 2005
    // one year in Army
    ServiceHistory CreateMockServiceHistoryForArmyToAirForce(int cftsArmyDays, int operationalArmyDays, int cftsAirforceDays, int operationalAirforceDays) {
        return new ServiceHistory() {
            @Override
            public LocalDate getHireDate() {
                return LocalDate.of(2004, 7, 1);
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

                return ImmutableSet.of(armyService, airForceService);

            }

            @Override
            public ServiceHistory filterServiceHistoryByEvents(List<String> eventList) {
                return null;
            }
        };
    }


    @Test
    public void ShouldReturnMultipleApplicableRuleConfigurations() {
        ConditionConfiguration mockConditionConfig = CreateMockConfigForArmyToAirForce("lumbar spondylosis"); // has to match condition mock name
        Condition mockCondition = new LumbarSpondylosisConditionMockWithOnsetDate(LocalDate.of(2010, 1, 1));
        ServiceHistory mockServiceHistory = CreateMockServiceHistoryForArmyToAirForce(150, 17, 150, 17);
        SopSupportCaseTrace mockCaseTrace = new SopSupportCaseTrace();
        ImmutableSet<ApplicableWearAndTearRuleConfiguration> results = mockConditionConfig.getApplicableRuleConfigurations(mockCondition.getSopPair().getConditionName(), mockCondition.getStartDate(), mockServiceHistory, mockCaseTrace);
        Assert.assertTrue(results.size() == 2);
    }

    @Test
    public void ShouldReturnNoMatchingConfigAsNoServicesBeforeOnsetDate() {
        ConditionConfiguration mockConditionConfig = CreateMockConfigForArmyToAirForce("lumbar spondylosis"); // has to match condition mock name
        Condition mockCondition = new LumbarSpondylosisConditionMockWithOnsetDate(LocalDate.of(2001, 1, 1));
        ServiceHistory mockServiceHistory = CreateMockServiceHistoryForArmyToAirForce(150, 17, 150, 17);
        SopSupportCaseTrace mockCaseTrace = new SopSupportCaseTrace();
        ImmutableSet<ApplicableWearAndTearRuleConfiguration> results = mockConditionConfig.getApplicableRuleConfigurations(mockCondition.getSopPair().getConditionName(), mockCondition.getStartDate(), mockServiceHistory, mockCaseTrace);
        Assert.assertTrue(results.size() == 0);
    }


    // Airforce is twice the days of Army, and BoP is twice the days of RH
    private static RuleConfigurationRepository createMockRulesConfigurationRepo(String conditionName, int seedCftsDays) {
        return new RuleConfigurationRepository() {
            @Override
            public ImmutableSet<RHRuleConfigurationItem> getRHItems() {
                return ImmutableSet.of(
                        new RHRuleConfigurationItem() {
                            @Override
                            public int getRequiredDaysOfOperationalService() {
                                return seedCftsDays / 10;
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
                            public ImmutableSet<FactorReference> getFactorRefObjects() {
                                return null;
                            }

                            @Override
                            public ImmutableSet<String> getMainFactorReferences() {
                                return ImmutableSet.of("9(1)");
                            }

                            @Override
                            public ServiceBranch getServiceBranch() {
                                return ServiceBranch.ARMY;
                            }

                            @Override
                            public Rank getRank() {
                                return Rank.OtherRank;
                            }

                            @Override
                            public int getRequiredCFTSDays() {
                                return seedCftsDays;
                            }
                        },
                        new RHRuleConfigurationItem() {
                            @Override
                            public int getRequiredDaysOfOperationalService() {
                                return (seedCftsDays * 2) / 2;
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
                            public ImmutableSet<FactorReference> getFactorRefObjects() {
                                return null;
                            }

                            @Override
                            public ImmutableSet<String> getMainFactorReferences() {
                                return ImmutableSet.of("9(1)");
                            }

                            @Override
                            public ServiceBranch getServiceBranch() {
                                return ServiceBranch.RAAF;
                            }

                            @Override
                            public Rank getRank() {
                                return Rank.OtherRank;
                            }

                            @Override
                            public int getRequiredCFTSDays() {
                                return seedCftsDays * 2;
                            }
                        }
                );
            }

            @Override
            public ImmutableSet<BoPRuleConfigurationItem> getBoPItems() {
                return ImmutableSet.of(
                        new BoPRuleConfigurationItem() {
                            @Override
                            public String getConditionName() {
                                return conditionName;
                            }

                            @Override
                            public String getInstrumentId() {
                                return null;
                            }

                            @Override
                            public ImmutableSet<FactorReference> getFactorRefObjects() {
                                return null;
                            }


                            @Override
                            public ImmutableSet<String> getMainFactorReferences() {
                                return ImmutableSet.of("1");
                            }

                            @Override
                            public ServiceBranch getServiceBranch() {
                                return ServiceBranch.ARMY;
                            }

                            @Override
                            public Rank getRank() {
                                return Rank.OtherRank;
                            }

                            @Override
                            public int getRequiredCFTSDays() {
                                return seedCftsDays * 2;
                            }
                        },
                        new BoPRuleConfigurationItem() {
                            @Override
                            public String getConditionName() {
                                return conditionName;
                            }

                            @Override
                            public String getInstrumentId() {
                                return null;
                            }

                            @Override
                            public ImmutableSet<FactorReference> getFactorRefObjects() {
                                return null;
                            }

                            @Override
                            public ImmutableSet<String> getMainFactorReferences() {
                                return ImmutableSet.of("9(1)");
                            }

                            @Override
                            public ServiceBranch getServiceBranch() {
                                return ServiceBranch.RAAF;
                            }

                            @Override
                            public Rank getRank() {
                                return Rank.OtherRank;
                            }

                            @Override
                            public int getRequiredCFTSDays() {
                                return seedCftsDays * 4;
                            }
                        }
                );
            }
        };
    }

    private static SoP createMockSoP(boolean isRH, String conditionName, String registerId)
    {
        return new SoP() {
            @Override
            public String getRegisterId() {
                return registerId;
            }

            @Override
            public InstrumentNumber getInstrumentNumber() {
                return null;
            }

            @Override
            public String getCitation() {
                return null;
            }

            @Override
            public ImmutableList<Factor> getAggravationFactors() {
                return null;
            }

            @Override
            public ImmutableList<Factor> getOnsetFactors() {
                return ImmutableList.of(
                        new Factor() {
                            @Override
                            public String getParagraph() {
                                return "9(1)";
                            }

                            @Override
                            public String getText() {
                                return "Mock SoP Factor 1";
                            }

                            @Override
                            public ImmutableSet<DefinedTerm> getDefinedTerms() {
                                return null;
                            }
                        }
                );
            }

            @Override
            public LocalDate getEffectiveFromDate() {
                return null;
            }

            @Override
            public Optional<LocalDate> getEndDate() {
                return Optional.empty();
            }

            @Override
            public StandardOfProof getStandardOfProof() {
                return (isRH ? StandardOfProof.ReasonableHypothesis : StandardOfProof.BalanceOfProbabilities);
            }

            @Override
            public ImmutableSet<ICDCode> getICDCodes() {
                return null;
            }

            @Override
            public String getConditionName() {
                return conditionName;
            }
        };
    }

    private static SoPPair createMockSopPair(String conditionName, String rhRegisterId, String bopRegisterId) {
        return new SoPPair(conditionName,createMockSoP(false,conditionName,bopRegisterId),createMockSoP(true,conditionName,rhRegisterId));
    }

    // Army, then RAAF.  Operational service is days / 10, occurs at start of time in branch of service.
    private static ServiceHistoryDto createMockServiceHistoryDto(LocalDate startDate, int daysInArmy, int daysInAirForce)
    {
        return new ServiceHistoryDto(
                new ServiceSummaryInfoDto(startDate),
                ImmutableList.of(
                        new ServiceDto(ServiceBranch.ARMY,EmploymentType.CFTS,startDate,startDate.plusDays(daysInArmy),Rank.OtherRank,
                                ImmutableList.of(
                                        new OperationalServiceDto(startDate,"OPERATIONAL","within specified area",startDate,startDate.plusDays(daysInArmy/10))
                                )),
                        new ServiceDto(ServiceBranch.RAAF,EmploymentType.CFTS,startDate.plusDays(daysInArmy),startDate.plusDays(daysInArmy + daysInAirForce),Rank.OtherRank,
                                ImmutableList.of(
                                        new OperationalServiceDto(startDate,"OPERATIONAL","within specified area",startDate.plusDays(daysInArmy),startDate.plusDays(daysInArmy).plusDays(daysInAirForce/10))
                                )
                )
        ));
    }

    private static ServiceHistoryDto createMockServiceHistoryDtoSingleBranch(LocalDate startDate, int daysInArmy)
    {
        return new ServiceHistoryDto(
                new ServiceSummaryInfoDto(startDate),
                ImmutableList.of(
                        new ServiceDto(ServiceBranch.ARMY,EmploymentType.CFTS,startDate,startDate.plusDays(daysInArmy),Rank.OtherRank,
                                ImmutableList.of(
                                        new OperationalServiceDto(startDate,"OPERATIONAL","within specified area",startDate,startDate.plusDays(daysInArmy/10))
                                )
                )));
    }

    private static ConditionDto createMockConditionDto(String conditionName, LocalDate onsetDate)
    {
        return new ConditionDto(conditionName,IncidentType.Onset,null,null,new OnsetDateRangeDto(onsetDate,onsetDate),null);
    }

    Predicate<Deployment> isOperationalMock = deployment -> deployment.getOperationName() == "OPERATIONAL";


    @Test
    public void TestMultipleBranchesOfService(){
        String conditionName = "lumbar spondylosis";
        LocalDate startOfServiceDate = LocalDate.of(2004,7,1);
        int seedDaysForRuleConfig = 100;
        int daysInArmy = 150;
        int daysInAirForce = 150;
        LocalDate onsetDate = startOfServiceDate.plusDays(daysInArmy).plusDays(daysInAirForce);

        // 10 days RH service, 100 days CFTS for RH, double for BoP
        RuleConfigurationRepository mockRepo = createMockRulesConfigurationRepo(conditionName,seedDaysForRuleConfig);

        SopSupportRequestDto mockRequest = new SopSupportRequestDto(createMockConditionDto(conditionName,onsetDate),createMockServiceHistoryDto(startOfServiceDate,daysInArmy,daysInAirForce));
        CaseTrace caseTrace = new SopSupportCaseTrace();
        RulesResult result = RulesResult.applyRules(mockRepo,mockRequest, ImmutableSet.of(createMockSopPair(conditionName,null,null)),isOperationalMock, caseTrace);


        Assert.assertTrue(result.getCaseTrace().isComplete());
        Assert.assertTrue(result.getRecommendation() == Recommendation.APPROVED);
    }

    RuleConfigurationRepository createMockEmptyRuleConfigRepo() {
        return new RuleConfigurationRepository() {
            @Override
            public ImmutableSet<RHRuleConfigurationItem> getRHItems() {
                return ImmutableSet.of();
            }

            @Override
            public ImmutableSet<BoPRuleConfigurationItem> getBoPItems() {
                return ImmutableSet.of();
            }
        };
    }

    @Test
    public void AcuteConditionRegressionTestForMultipleBranchesOfService() {
        String conditionName = "external burn";
        LocalDate startOfServiceDate = LocalDate.of(2004,7,1);
        int daysInArmy = 150;
        int daysInAirForce = 150;
        LocalDate onsetDate = LocalDate.of(2004,8,1); // during army service

        // 10 days RH service, 100 days CFTS for RH, double for BoP
        RuleConfigurationRepository mockRepo = createMockEmptyRuleConfigRepo();

        SopSupportRequestDto mockRequest = new SopSupportRequestDto(createMockConditionDto(conditionName,onsetDate),createMockServiceHistoryDto(startOfServiceDate,daysInArmy,daysInAirForce));
        CaseTrace caseTrace = new SopSupportCaseTrace();
        RulesResult result = RulesResult.applyRules(mockRepo,mockRequest, ImmutableSet.of(createMockSopPair(conditionName,"F2017C00862","F2017C00861")),isOperationalMock, caseTrace);

        Assert.assertTrue(result.getCaseTrace().isComplete());
        Assert.assertTrue(result.getRecommendation() == Recommendation.APPROVED);
    }

    @Test
    public void TestPreconditionsFailed() {
        String conditionName = "external burn";
        LocalDate startOfServiceDate = LocalDate.of(2019,7,1);
        int daysInArmy = 150;
        int daysInAirForce = 150;
        LocalDate onsetDate = LocalDate.of(2004,8,1); // during army service

        // 10 days RH service, 100 days CFTS for RH, double for BoP
        RuleConfigurationRepository mockRepo = createMockEmptyRuleConfigRepo();

        SopSupportRequestDto mockRequest = new SopSupportRequestDto(createMockConditionDto(conditionName,onsetDate),createMockServiceHistoryDto(startOfServiceDate,daysInArmy,daysInAirForce));
        CaseTrace caseTrace = new SopSupportCaseTrace();
        RulesResult result = RulesResult.applyRules(mockRepo,mockRequest, ImmutableSet.of(createMockSopPair(conditionName,"F2017C00862","F2017C00861")),isOperationalMock, caseTrace);

        Assert.assertTrue(result.getCaseTrace().isComplete());
        Assert.assertTrue(result.getCaseTrace().getReasonings().containsKey(ReasoningFor.ABORT_PROCESSING));
    }


    @Test
    public void TestSingleBranchOfService() {
        String conditionName = "lumbar spondylosis";
        LocalDate startOfServiceDate = LocalDate.of(2004,7,1);
        int seedDaysForRuleConfig = 100;
        int daysInArmy = 150;
        LocalDate onsetDate = startOfServiceDate.plusDays(daysInArmy - 10);

        // 10 days RH service, 100 days CFTS for RH, double for BoP
        RuleConfigurationRepository mockRepo = createMockRulesConfigurationRepo(conditionName,seedDaysForRuleConfig);

        SopSupportRequestDto mockRequest = new SopSupportRequestDto(createMockConditionDto(conditionName,onsetDate),createMockServiceHistoryDtoSingleBranch(startOfServiceDate,daysInArmy));
        CaseTrace caseTrace = new SopSupportCaseTrace();
        RulesResult result = RulesResult.applyRules(mockRepo,mockRequest, ImmutableSet.of(createMockSopPair(conditionName,null,null)),isOperationalMock, caseTrace);


        Assert.assertTrue(result.getCaseTrace().isComplete());
        Assert.assertTrue(result.getRecommendation() == Recommendation.APPROVED);
    }
}




