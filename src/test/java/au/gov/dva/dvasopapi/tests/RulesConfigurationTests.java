package au.gov.dva.dvasopapi.tests;


import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.sopsupport.ruleconfiguration.CsvRuleConfigurationRepository;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.IOException;

public class RulesConfigurationTests {

    @Test
    public void loadRulesConfig() throws IOException {
        byte[] rhCsv = Resources.toByteArray(Resources.getResource("rulesConfiguration/RH.csv"));
        byte[] boPCsv = Resources.toByteArray(Resources.getResource("rulesConfiguration/BoP.csv"));
        RuleConfigurationRepository underTest = new CsvRuleConfigurationRepository(rhCsv, boPCsv);
        assert(underTest.getRHItems().size() == 17 && underTest.getBoPItems().size() == 12);
        underTest.getRHItems().forEach(rhRuleConfigurationItem -> System.out.println(rhRuleConfigurationItem));
        underTest.getBoPItems().forEach(rhRuleConfigurationItem -> System.out.println(rhRuleConfigurationItem));
    }
}
