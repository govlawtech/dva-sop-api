package au.gov.dva.dvasopapi.tests;


import au.gov.dva.sopapi.interfaces.RuleConfigurationRepository;
import au.gov.dva.sopapi.sopsupport.ruleconfiguration.CsvRuleConfigurationRepository;
import com.google.common.io.Resources;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RulesConfigurationTests {

    @Test
    @Ignore
    public void loadRulesConfig() throws IOException {
        byte[] rhCsv = Files.readAllBytes(Paths.get("../../dva-sop-api-rule-config/rh.csv"));
        byte[] boPCsv = Files.readAllBytes(Paths.get("../../dva-sop-api-rule-config/bop.csv"));
        RuleConfigurationRepository underTest = new CsvRuleConfigurationRepository(rhCsv, boPCsv);
        assert(underTest.getRHItems().size() > 0 && underTest.getBoPItems().size() > 0);
        underTest.getRHItems().forEach(rhRuleConfigurationItem -> System.out.println(rhRuleConfigurationItem));
        underTest.getBoPItems().forEach(rhRuleConfigurationItem -> System.out.println(rhRuleConfigurationItem));

        underTest.getRHItems().forEach(rhRuleConfigurationItem -> {
            rhRuleConfigurationItem.getFactorReferences().forEach(factorReference -> System.out.println(factorReference.getMainFactorReference()));
        });
    }
}
