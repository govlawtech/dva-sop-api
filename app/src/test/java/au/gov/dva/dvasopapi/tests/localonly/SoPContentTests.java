package au.gov.dva.dvasopapi.tests.localonly;

import au.gov.dva.dvasopapi.tests.categories.IntegrationTest;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import com.google.common.collect.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import scala.Int;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SoPContentTests {

    private static Repository repository = new AzureStorageRepository("UseDevelopmentStorage=true");
    private static ImmutableSet<SoP> soPS  =  repository.getAllSops();


    @Category(IntegrationTest.class)
    @Test
    public void NoAggravationFactorsInOnset()
    {

        soPS.stream().forEach(soP -> {
            List<Factor> suspectOnsetFactors =  soP.getOnsetFactors().stream()
                    .filter(t -> t.getText().contains("inability to obtain appropriate clinical") || t.getText().contains("clinical worsening"))
                    .collect(Collectors.toList());

            if (suspectOnsetFactors.size() > 1)
            {
                System.out.println(soP.getRegisterId() + ": " + soP.getConditionName() + ": " + suspectOnsetFactors.size());
                suspectOnsetFactors.stream().forEach(f -> System.out.println(f.getParagraph()));
            }
        });
    }

    @Category(IntegrationTest.class)
    @Test
    public void NoFootNotesInFactorText()
    {
        soPS.stream().forEach(soP -> {
            Iterable<Factor> factors = Iterables.concat(soP.getOnsetFactors(),soP.getAggravationFactors());
            for (Factor f : factors) {
                if (f.getText().contains("Veterans' Entitlements Act 1986") || f.getText().contains("Statement of Principles"))
                {
                    System.out.println(soP.getRegisterId() + ": " + f.getText());
                }
            } 
        });
    }
}
