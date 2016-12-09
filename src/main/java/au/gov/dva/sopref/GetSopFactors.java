package au.gov.dva.sopref;

import au.gov.dva.sopref.dtos.SoPDto;
import au.gov.dva.sopref.dtos.SoPRefDto;
import au.gov.dva.sopref.exceptions.DvaSopApiError;
import au.gov.dva.sopref.interfaces.model.ICDCode;
import au.gov.dva.sopref.interfaces.model.IncidentType;
import au.gov.dva.sopref.interfaces.model.SoP;
import au.gov.dva.sopref.interfaces.model.StandardOfProof;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.stream.Collectors;

public class GetSopFactors {


    public static String buildSopRefJsonResponse(ImmutableSet<SoP> matchingSops, IncidentType incidentType, StandardOfProof standardOfProof) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<SoPDto> sopDtos = matchingSops.stream()
                .map(s -> SoPDto.fromSop(s,standardOfProof,incidentType))
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
}
