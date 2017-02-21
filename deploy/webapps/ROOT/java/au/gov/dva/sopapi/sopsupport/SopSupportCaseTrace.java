package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.interfaces.CaseTrace;

public class SopSupportCaseTrace implements CaseTrace {

    private StringBuilder sb;

    public SopSupportCaseTrace(String caseId)
    {
        sb = new StringBuilder("Case ID: " + caseId);
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    @Override
    public void addTrace(String msg) {
        sb.append(msg + "\r\n");

    }
}
