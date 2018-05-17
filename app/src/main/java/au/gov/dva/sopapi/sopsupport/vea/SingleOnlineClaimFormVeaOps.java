package au.gov.dva.sopapi.sopsupport.vea;

import au.gov.dva.sopapi.interfaces.model.SingleOnlineClaimFormVeaOp;
import au.gov.dva.sopapi.sopref.data.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

public class SingleOnlineClaimFormVeaOps {
    public static ImmutableList<SingleOnlineClaimFormVeaOp> fromYaml(String yamlRefData) throws IOException {

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        JsonNode n = om.readTree(yamlRefData);
        ImmutableList<JsonNode> serviceRegions = JsonUtils.getChildrenOfArrayNode((ArrayNode) n.findPath("referenceData").findPath("serviceRegions"));
        ImmutableList<SingleOnlineClaimFormVeaOp> deserialised = serviceRegions.stream()
                .map(jsonNode -> {
                            return new SingleOnlineClaimFormOpImpl(
                                    jsonNode.findValue("operation").asText(),
                                    LocalDate.parse(jsonNode.findValue("startDate").asText(), DateTimeFormatter.ISO_LOCAL_DATE),
                                    jsonNode.hasNonNull("endDate") ? Optional.of(LocalDate.parse(jsonNode.findValue("endDate").asText(), DateTimeFormatter.ISO_LOCAL_DATE)) : Optional.empty(),
                                    jsonNode.findValue("isHazardous").asBoolean(),
                                    jsonNode.findValue("isMrcaNonWarlike").asBoolean(),
                                    jsonNode.findValue("isMrcaWarlike").asBoolean(),
                                    jsonNode.findValue("isOperational").asBoolean(),
                                    jsonNode.findValue("isPeacekeeping").asBoolean(),
                                    jsonNode.findValue("isWarlike").asBoolean());

                        }
                )
                .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));


        return deserialised;


    }



}

