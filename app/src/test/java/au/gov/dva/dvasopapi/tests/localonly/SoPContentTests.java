package au.gov.dva.dvasopapi.tests.localonly;

import au.gov.dva.dvasopapi.tests.categories.IntegrationTest;
import au.gov.dva.sopapi.interfaces.Repository;
import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.data.AzureStorageRepository;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;
import java.util.stream.Collectors;

public class SoPContentTests {




    @Category(IntegrationTest.class)
    @Test
    public void NoAggravationFactorsInOnset()
    {
        Repository repository = new AzureStorageRepository("UseDevelopmentStorage=true");
        ImmutableSet<SoP> soPS  =  repository.getAllSops();
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
}
