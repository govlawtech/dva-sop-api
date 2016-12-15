package au.gov.dva.sopref;

import au.gov.dva.exceptions.DvaSopApiError;
import au.gov.dva.interfaces.model.*;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.sopref.SoPDto;
import au.gov.dva.sopapi.dtos.sopref.SoPRefDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.stream.Collectors;

public class SoPs {


    public static String buildSopRefJsonResponse(ImmutableSet<SoP> matchingSops, IncidentType incidentType, StandardOfProof standardOfProof) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<SoPDto> sopDtos = matchingSops.stream()
                .map(s -> DtoTransformations.fromSop(s,standardOfProof,incidentType))
                .collect(Collectors.toList());

        SoPRefDto dtoToReturn = new SoPRefDto(sopDtos);
        String jsonString = null;
        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToReturn);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiError(e);
        }
        return jsonString;
    }

    public static ImmutableSet<SoP> getMatchingSops(String conditionName, ICDCode icdCode, ImmutableSet<SoP> toMatch)
    {
        ImmutableSet<SoP> matchingOnIcdCode = ImmutableSet.copyOf(
                toMatch.stream().filter(
                        soP -> soP.getICDCodes().stream().anyMatch(
                                icdCodeInSop -> icdCodeInSop.equals(icdCode))).collect(Collectors.toSet()));

        if (!matchingOnIcdCode.isEmpty())
            return matchingOnIcdCode;

        ImmutableSet<SoP> matchingOnName = ImmutableSet.copyOf(
                toMatch.stream().filter(soP -> soP.getConditionName().equalsIgnoreCase(conditionName))
                        .collect(Collectors.toList()));

        return matchingOnName;
    }


    public static ImmutableSet<SoPPair> groupSopsToPairs(ImmutableSet<SoP> allSops)
    {
        return null;
    }


}
