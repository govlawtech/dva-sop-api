package au.gov.dva.sopref.interfaces.model.casesummary;

import au.gov.dva.sopref.interfaces.model.*;


public interface CaseSummaryModel {
    /*
    * INPUTS:
    * Condition
    * - Condition name
    * - Condition ICD code
    * - Acute condition / Accumulated over time (wear and tear)
    * - Date of onset
    * - Date of aggravation (range)
    *
    * Service history
    * - Date of enlistment
    * - Date of separation
    * - Original hire date
    * - Services history (list)
    * - - Service name (eg. RAN)
    * - - Service type (eg. Regular/Permanent force)
    * - - Start date
    * - - End date
    *
    * Operations history (list)
    * - Operation name
    * - Start date
    * - End date
    * - "Specified Area" date (CADF - MRCA 6(1)(b))
    *
    * OUTPUTS:
    * SOP
    * - SOP title (Statement of Principles concerning JOINT INSTABILITY No. 32 of 2010)
    * - SOP type (BOP)
    * - SOP URL
    * - Applicable SOP factors (list)
    * - Factors connected to service (list)
    * - Progress towards threshold (wear and tear factors that include a threshold level of work and aren't satisfied)
    *
    * */

    Condition getCondition();
    ServiceHistory getServiceHistory();
    SoP getSop();
}
