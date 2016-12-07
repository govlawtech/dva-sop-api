package au.gov.dva.sopref.data.sops;

import au.gov.dva.sopref.interfaces.model.ICDCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class BasicICDCode implements au.gov.dva.sopref.interfaces.model.ICDCode {
    private final String version;
    private final String code;

    public static final String ICD_CODE_VERSION = "version";
    public static final String ICD_CODE = "code";

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





    public static JsonNode toJson(ICDCode toSerialize) {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node = objectMapper.createObjectNode();
        node.put(ICD_CODE_VERSION,toSerialize.getVersion());
        node.put(ICD_CODE,toSerialize.getCode());
        return node;
    }
}


