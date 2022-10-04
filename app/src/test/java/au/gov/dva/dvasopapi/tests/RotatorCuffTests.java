package au.gov.dva.dvasopapi.tests;


import au.gov.dva.dvasopapi.tests.mocks.ConditionMock;
import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.dtos.*;
import au.gov.dva.sopapi.interfaces.ApplicableWearAndTearRuleConfiguration;
import au.gov.dva.sopapi.interfaces.BoPRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.RHRuleConfigurationItem;
import au.gov.dva.sopapi.interfaces.model.*;
import au.gov.dva.sopapi.sopref.data.sops.StoredSop;
import au.gov.dva.sopapi.sopsupport.SopSupportCaseTrace;
import au.gov.dva.sopapi.sopsupport.processingrules.ApplicableRuleConfigurationImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.RulesResult;
import au.gov.dva.sopapi.sopsupport.processingrules.ServiceHistoryImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.ServiceImpl;
import au.gov.dva.sopapi.sopsupport.processingrules.rules.RotatorCuffSyndromeRule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

public class RotatorCuffTests {



    ApplicableWearAndTearRuleConfiguration buildMockConfig() {

        BoPRuleConfigurationItem bopConfig = new BoPRuleConfigurationItem() {
            @Override
            public String getConditionName() {
                return "rotator cuff syndrome";
            }

            @Override
            public String getInstrumentId() {
                return "foo bar bop id";
            }

            @Override
            public ImmutableSet<FactorReference> getFactorRefObjects() {
                return ImmutableSet.of(new FactorReference() {
                    @Override
                    public String getMainFactorReference() {
                        return "6(b)";

                    }

                    @Override
                    public Optional<String> getFactorPartReference() {
                        return Optional.empty();
                    }
                });
            }

            @Override
            public ServiceBranch getServiceBranch() {
                return ServiceBranch.RAN;
            }

            @Override
            public Rank getRank() {
                return Rank.Officer;
            }

            @Override
            public int getRequiredCFTSDays() {
                return 210;
            }
        };

        RHRuleConfigurationItem rhConfig = new RHRuleConfigurationItem() {
            @Override
            public int getRequiredDaysOfOperationalService() {
                return 21;
            }

            @Override
            public Optional<Integer> getYearsLimitForOperationalService() {
                return Optional.empty();
            }

            @Override
            public String getConditionName() {
                return "rotator cuff syndrome";
            }

            @Override
            public String getInstrumentId() {
                return "MOCKROTATORCUFFINSTRUMENTID";
            }

            @Override
            public ImmutableSet<FactorReference> getFactorRefObjects() {
                return ImmutableSet.of(new FactorReference() {
                    @Override
                    public String getMainFactorReference() {
                        return "6(b)";
                    }

                    @Override
                    public Optional<String> getFactorPartReference() {
                        return Optional.empty();
                    }
                });
            }

            @Override
            public ServiceBranch getServiceBranch() {
                return ServiceBranch.RAN;
            }

            @Override
            public Rank getRank() {
                return Rank.Officer;
            }

            @Override
            public int getRequiredCFTSDays() {
                return 120;
            }
        };

        return new ApplicableRuleConfigurationImpl("rotator cuff syndrome", rhConfig, Optional.of(bopConfig));

    }

    SoP deserialiseSopFromResources(String resourcePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        URL sopUrl = Resources.getResource(resourcePath);
        String sopString = Resources.toString(sopUrl, StandardCharsets.UTF_8);
        JsonNode jsonNode = objectMapper.readTree(sopString);
        SoP result = StoredSop.fromJson(jsonNode);
        return result;
    }

    SoPPair buildApplicableSopPair() throws IOException {
        SoP rhSop = deserialiseSopFromResources("rotatorCuffTests/F2021C00080");
        SoP bopSop = deserialiseSopFromResources("rotatorCuffTests/F2021C00081");
        return new SoPPair(bopSop,rhSop);
    }

    ServiceHistory buildServiceHistory(LocalDate separationDate, int lengthOfService) {
        LocalDate startDate = separationDate.minusDays(lengthOfService - 1); // assume in service on separation date
        Service service = new ServiceImpl(ServiceBranch.RAN,EmploymentType.CFTS,Rank.Officer,startDate,Optional.of(separationDate),ImmutableSet.of());
        return new ServiceHistoryImpl(LocalDate.of(2020,1,1),ImmutableSet.of(service));
    }

    ServiceHistory buildDualServiceHistory(LocalDate separationDate, int lengthOfServiceInArmy, int lengthOfServiceInNavy) {
        LocalDate startDate = separationDate.minusDays(lengthOfServiceInArmy + lengthOfServiceInNavy - 1); // assume in service on separation date
        Service serviceInArmy = new ServiceImpl(ServiceBranch.ARMY,EmploymentType.CFTS,Rank.Officer,startDate,Optional.of(separationDate),ImmutableSet.of());
        Service serviceInNavy = new ServiceImpl(ServiceBranch.RAN,EmploymentType.CFTS,Rank.Officer,startDate.plusDays(lengthOfServiceInArmy),Optional.of(separationDate),ImmutableSet.of());
        return new ServiceHistoryImpl(LocalDate.of(2000,1,1),ImmutableSet.of(serviceInArmy,serviceInNavy));
    }

    boolean isScenarioSatisfied(LocalDate separationDate, int inclusiveDaysAsNavyOfficer, LocalDate conditionOnset) throws IOException {
        RotatorCuffSyndromeRule underTest = new RotatorCuffSyndromeRule(buildMockConfig());
        SoPPair soPPair = buildApplicableSopPair();
        Condition mockCondition = new ConditionMock(soPPair,conditionOnset,null,underTest);
        ServiceHistory serviceHistory = buildServiceHistory(separationDate, inclusiveDaysAsNavyOfficer);
        CaseTrace caseTrace = new SopSupportCaseTrace();
        Optional<SoP> applicableSop = underTest.getApplicableSop(mockCondition,serviceHistory,deployment -> false,caseTrace);
        if (applicableSop.isPresent()) {
            ImmutableList<FactorWithSatisfaction> results = underTest.getSatisfiedFactors(mockCondition, soPPair.getBopSop(), serviceHistory, caseTrace);
            boolean isSatisified = results.stream().anyMatch(f -> f.isSatisfied());
            return isSatisified;
        }
        return false;
    }

    @Test
    public void RotatorCuffOnsets30DaysAfterServiceEnds() throws IOException {

        LocalDate separationdate = LocalDate.of(2020,12,31);
        LocalDate onsetDate = separationdate.plusDays(31);
        int daysAsNavyOfficer = 210;
        boolean result = isScenarioSatisfied(separationdate,daysAsNavyOfficer,onsetDate);
        Assert.assertTrue(result);
    }

    @Test
    public void RotatorCuffOnsets31DaysAfterServiceEnds() throws IOException {
        LocalDate separationdate = LocalDate.of(2020,12,31);
        LocalDate onsetDate = separationdate.plusDays(32);
        int daysAsNavyOfficer = 210;
        boolean result = isScenarioSatisfied(separationdate,daysAsNavyOfficer,onsetDate);
        Assert.assertFalse(result);
    }

    @Test
    public void RotatorCuffOnsetsDuringServiceButServiceTooShort() throws IOException {

        LocalDate separationdate = LocalDate.of(2020,12,31);
        LocalDate onsetDate = separationdate;
        int daysAsNavyOfficer = 209;
        boolean result = isScenarioSatisfied(separationdate,daysAsNavyOfficer,onsetDate);
        Assert.assertFalse(result);
    }

    @Test
    public void RotatorCuffOnsetsDuringServiceOfSufficentLength() throws IOException {

        LocalDate separationdate = LocalDate.of(2020,12,31);
        LocalDate onsetDate = separationdate;
        int daysAsNavyOfficer = 210;
        boolean result = isScenarioSatisfied(separationdate,daysAsNavyOfficer,onsetDate);
        Assert.assertTrue(result);
    }

    @Test
    public void ServiceOfSuffcientLengthButOnsetBeforeService() throws IOException {
        LocalDate separationdate = LocalDate.of(2020,12,31);
        LocalDate onsetDate = separationdate.minusDays(370);
        int daysAsNavyOfficer = 210;
        boolean result = isScenarioSatisfied(separationdate,daysAsNavyOfficer,onsetDate);
        Assert.assertFalse(result);
    }


}
