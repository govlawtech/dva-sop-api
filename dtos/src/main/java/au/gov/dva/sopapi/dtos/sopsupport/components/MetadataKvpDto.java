package au.gov.dva.sopapi.dtos.sopsupport.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class MetadataKvpDto {
    @JsonProperty("key")
    private final String key;

    @JsonProperty("value")
    private final String value;
    public MetadataKvpDto(@JsonProperty String key, @JsonProperty String value) {
        this.key = key;
        this.value = value;
    }
}
