package au.gov.dva.dvasopapi.tests;

import au.gov.dva.sopapi.interfaces.model.DefinedTerm;
import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.FactorWithSatisfaction;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Assert;
import org.junit.Test;
import scala.Tuple2;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SubFactorPartsTests {

    @Test
    public void subPartApplied() {

        Factor mockFactor = new Factor() {
            @Override
            public String getParagraph() {
                return "9(14)";
            }

            @Override
            public String getText() {
                return "for osteoarthritis of a joint of the lower limb only:\\r\\n(a) having:\\r\\n(i) an amputation involving either leg; or\\r\\n(ii) an asymmetric gait;\\r\\nfor at least three years before the clinical onset of osteoarthritis\\r\\nin that joint;\\r\\n(b) lifting loads of at least 20 kilograms while bearing weight\\r\\nthrough the affected joint to a cumulative total of at least\\r\\n100 000 kilograms within any ten year period before the clinical\\r\\nonset of osteoarthritis in that joint;\\r\\n(c) carrying loads of at least 20 kilograms while bearing weight\\r\\nthrough the affected joint to a cumulative total of at least 3 800\\r\\nhours within any ten year period before the clinical onset of\\r\\nosteoarthritis in that joint;\\r\\n(d) ascending or descending at least 150 stairs or rungs of a ladder\\r\\nper day, on more days than not, for a continuous period of at\\r\\nleast two years before the clinical onset of osteoarthritis in that\\r\\njoint; or\\r\\n(e) having increased bone mineral density before the clinical onset\\r\\nof osteoarthritis in that joint";
            }

            @Override
            public ImmutableSet<DefinedTerm> getDefinedTerms() {
                return null;
            }
        };

        ImmutableList<Factor> mockFactors = ImmutableList.of(mockFactor);

        ImmutableSet<String> mockFactorParas = ImmutableSet.of("9(14)(b)");

        Function<String, Tuple2<String,String>> splitter = s -> new Tuple2<>("9(14)","(b)");

        BiFunction<String,Factor,Optional<String>> extractor = (subParagraphReference, factor) -> Optional.of("(b) lifting loads of at least 20 kilograms while bearing weight\\r\\nthrough the affected joint to a cumulative total of at least\\r\\n100 000 kilograms within any ten year period before the clinical\\r\\nonset of osteoarthritis in that joint;");

        ImmutableList<FactorWithSatisfaction> result = ProcessingRuleFunctions.withSatisfiedFactors(mockFactors,mockFactorParas,splitter,extractor);


        Assert.assertTrue(result.get(0).getApplicablePart().contentEquals("(b) lifting loads of at least 20 kilograms while bearing weight\\r\\nthrough the affected joint to a cumulative total of at least\\r\\n100 000 kilograms within any ten year period before the clinical\\r\\nonset of osteoarthritis in that joint;"));


    }


}
