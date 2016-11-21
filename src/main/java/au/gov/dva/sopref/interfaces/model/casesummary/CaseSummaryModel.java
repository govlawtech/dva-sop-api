package au.gov.dva.sopref.interfaces.model.casesummary;

import au.gov.dva.sopref.interfaces.model.Factor;
import au.gov.dva.sopref.interfaces.model.Operation;
import au.gov.dva.sopref.interfaces.model.SoP;
import com.google.common.collect.ImmutableSet;

import java.time.LocalDate;

public interface CaseSummaryModel {
    // TB todo: add members here for data necessary to generate case summary document

    /*
    * Need the following:
    * Condition
    * Date of onset
    * Service history (list of operations)
    * SOP title (Statement of Principles concerning JOINT INSTABILITY No. 32 of 2010)
    * SOP type (BOP)
    * SOP link
    * Factors connected to service
    *
    * */

//    String getCondition();
//    LocalDate getConditionOnsetDate();
//    ImmutableSet<Operation> getServiceHistory();
//    SoP getSop();
//    ImmutableSet<Factor> getFactorsConnectedToService();
}
