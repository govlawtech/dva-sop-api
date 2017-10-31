package au.gov.dva.sopapi.sopsupport.processingrules.rules;

import au.gov.dva.sopapi.dtos.ReasoningFor;
import au.gov.dva.sopapi.interfaces.CaseTrace;
import au.gov.dva.sopapi.interfaces.model.Condition;
import au.gov.dva.sopapi.interfaces.model.Service;
import au.gov.dva.sopapi.interfaces.model.ServiceHistory;
import au.gov.dva.sopapi.sopsupport.processingrules.ProcessingRuleFunctions;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

public class RulePreconditions {
    public static boolean isServiceHistoryInternallyConsistent(ServiceHistory serviceHistory, CaseTrace caseTrace)
    {
        LocalDate earliestStartDate = serviceHistory.getServices().stream()
                .sorted(Comparator.comparing(Service::getStartDate))
                .findFirst().get().getStartDate();
        if (serviceHistory.getHireDate().isAfter(earliestStartDate)) {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, "The service history begins before the hire date, therefore this service history is corrupt data and an applicable SoP cannot be determined.");
            return false;
        }
        return true;
    }

    public static boolean serviceExistsBeforeConditionOnset(ServiceHistory serviceHistory, Condition condition, CaseTrace caseTrace)
    {
        Optional<Service> serviceDuringWhichConditionStarts = ProcessingRuleFunctions.identifyCFTSServiceDuringOrAfterWhichConditionOccurs(serviceHistory.getServices(), condition.getStartDate(), caseTrace);
        if (!serviceDuringWhichConditionStarts.isPresent()) {
            caseTrace.addReasoningFor(ReasoningFor.ABORT_PROCESSING, "Cannot find any Service during or after which the condition started, therefore there is no applicable SoP.");
            return false;
        }
        return true;

    }


}
