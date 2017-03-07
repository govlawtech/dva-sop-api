package au.gov.dva.sopapi.sopref;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.SoPFactorsResponse;
import au.gov.dva.sopapi.dtos.sopref.SoPReferenceResponse;
import au.gov.dva.sopapi.exceptions.DvaSopApiError;
import au.gov.dva.sopapi.interfaces.model.ICDCode;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SoPs {

    static Logger logger = LoggerFactory.getLogger(SoPs.class);

    public static String buildSopRefJsonResponse(ImmutableSet<SoP> matchingSops, IncidentType incidentType, StandardOfProof standardOfProof) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<SoPFactorsResponse> sopFactorsResponses = matchingSops.stream()
                .map(s -> DtoTransformations.fromSop(s,standardOfProof,incidentType))
                .collect(Collectors.toList());

        SoPReferenceResponse dtoToReturn = new SoPReferenceResponse(sopFactorsResponses);
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

       Stream<Optional<SoPPair>> pairs = groupedByName.keySet()
               .stream().map(conditionName -> {

                   Optional<SoP> bopSop = groupedByName.get(conditionName).stream().filter(soP -> soP.getStandardOfProof() == StandardOfProof.BalanceOfProbabilities).findFirst();
                   Optional<SoP> rhSop = groupedByName.get(conditionName).stream().filter(soP -> soP.getStandardOfProof() == StandardOfProof.ReasonableHypothesis).findFirst();
                   if (bopSop.isPresent() && rhSop.isPresent()) {
                       return Optional.of(new SoPPair(bopSop.get(), rhSop.get()));
                   }
                   else {
                       logger.error(String.format("No complete SoP pair for condition: %s", conditionName));
                       return Optional.empty();
                   }
               });

       List<SoPPair> wherePresent = pairs.filter(p -> p.isPresent()).map(s -> s.get()).collect(Collectors.toList());
       return ImmutableSet.copyOf(wherePresent);

    }




}
