package au.gov.dva.sopapi.systemtests;

public class TestCaseResult {
    public final String fileName;
    public final Boolean passed;
    public final String log;

    public TestCaseResult(String fileName, Boolean passed, String log)
    {

        this.fileName = fileName;
        this.passed = passed;
        this.log = log;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TestCaseResult{");
        sb.append("fileName='").append(fileName).append('\'');
        sb.append(", passed=").append(passed);
        sb.append(", log='").append("\r\n").append(log).append('\'');
        sb.append('}');
        return sb.toString();
    }
}