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
        return "TestCaseResult{" +
                "fileName='" + fileName + '\'' +
                ", passed=" + passed  +
                "," + "\r\n" + "log='" + log + '\'' +
                '}';
    }
}