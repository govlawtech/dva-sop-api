package au.gov.dva.sopapi.sopref;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.SoPDto;
import au.gov.dva.sopapi.dtos.sopref.SoPRefDto;
import au.gov.dva.sopapi.exceptions.DvaSopApiError;
import au.gov.dva.sopapi.interfaces.model.ICDCode;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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
       Map<String,List<SoP>> groupedByName =  allSops.stream().collect(Collectors.groupingBy(sop -> sop.getConditionName()));

       assert(groupedByName.values().stream().allMatch(soPS -> soPS.size() == 2));

       List<SoPPair> pairs = groupedByName.values()
               .stream().map(soPS -> {
                   Optional<SoP> bopSop = soPS.stream().filter(soP -> soP.getStandardOfProof() == StandardOfProof.BalanceOfProbabilities).findFirst();
                   assert(bopSop.isPresent());
                   Optional<SoP> rhSop = soPS.stream().filter(soP -> soP.getStandardOfProof() == StandardOfProof.ReasonableHypothesis).findFirst();
                   assert(rhSop.isPresent());
                   return new SoPPair(bopSop.get(),rhSop.get());
               })
               .collect(Collectors.toList());

       return ImmutableSet.copyOf(pairs);

    }




}
