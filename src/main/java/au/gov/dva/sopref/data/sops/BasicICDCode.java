package au.gov.dva.sopref.data.sops;

import au.gov.dva.sopref.interfaces.model.ICDCode;

public class BasicICDCode implements au.gov.dva.sopref.interfaces.model.ICDCode {
    private final String version;
    private final String code;

    public BasicICDCode(String version, String code) {

        this.version = version;
        this.code = code;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "BasicICDCode{" +
                "version='" + version + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
