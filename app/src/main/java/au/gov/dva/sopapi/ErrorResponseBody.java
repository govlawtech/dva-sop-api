package au.gov.dva.sopapi;

import au.gov.dva.sopapi.dtos.DvaSopApiDtoRuntimeException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class ErrorResponseBody {

    @JsonProperty("id")
    private final String id;

    @JsonProperty("code")
    private final String code;

    @JsonProperty("detail")
    private final String detail;

    @JsonProperty("source")
    private final String source;

    @JsonCreator
    public ErrorResponseBody(@JsonProperty("id") String id, @JsonProperty("code") String code, @JsonProperty("detail") String detail, @JsonProperty("source") String source)
    {

        this.id = id;
        this.code = code;
        this.detail = detail;
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDetail() {
        return detail;
    }

    public String getSource() {
        return source;
    }

    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());
        String jsonString = null;

        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter()
                    .with(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                    .writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiDtoRuntimeException(e);
        }

        return jsonString;
    }
}
