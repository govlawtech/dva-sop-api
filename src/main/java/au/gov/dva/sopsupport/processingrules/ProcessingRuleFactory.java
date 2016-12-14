package au.gov.dva.sopsupport.processingrules;

import au.gov.dva.interfaces.ProcessingRule;
import au.gov.dva.interfaces.model.Condition;

public class ProcessingRuleFactory {
    public static ProcessingRule getRule(Condition condition) {
        String conditionName = condition.getSopPair().getConditionName();

        switch (conditionName)
        {
            case "lumbar spondylosis" : return new LumbarSpondylosisRule();
            default: return null; // todo: figure out default case - probably type test for acute and aggravated
        }
    }
}
