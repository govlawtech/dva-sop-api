package au.gov.dva.sopapi.dtos.sopref;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ICDCodeDto {

    @JsonProperty("version")
    private final String _version;

    @JsonProperty("code")
    private final String _code;



    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ICDCodeDto(@JsonProperty("version") String version, @JsonProperty("code") String code)
    {
        _version = version;
        _code = code;
    }

    public String get_version() {
        return _version;
    }

    public String get_code() {
        return _code;
    }
}
