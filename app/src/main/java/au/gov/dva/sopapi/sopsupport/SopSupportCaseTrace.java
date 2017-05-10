package au.gov.dva.sopapi.sopsupport;

import au.gov.dva.sopapi.interfaces.CaseTrace;
import scala.util.Properties;

public class SopSupportCaseTrace implements CaseTrace {

    private StringBuilder sb;

    public SopSupportCaseTrace(String caseId)
    {
        sb = new StringBuilder(String.format("Case ID: %s%n", caseId));
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    @Override
    public void addTrace(String msg) {
        sb.append(msg + Properties.lineSeparator());

    }

    @Override
    public String getTraces() {
        return sb.toString();
    }
}
