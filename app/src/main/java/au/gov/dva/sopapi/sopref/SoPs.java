package au.gov.dva.sopapi.sopref;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.DefinedTerm;
import au.gov.dva.sopapi.dtos.sopref.FactorDto;
import au.gov.dva.sopapi.dtos.sopref.SoPFactorsResponse;
import au.gov.dva.sopapi.dtos.sopref.SoPReferenceResponse;
import au.gov.dva.sopapi.exceptions.DvaSopApiRuntimeException;
import au.gov.dva.sopapi.interfaces.CuratedTextRepository;
import au.gov.dva.sopapi.interfaces.model.ICDCode;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.util.Properties;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SoPs {

    static Logger logger = LoggerFactory.getLogger(SoPs.class);

    public static String buildSopRefJsonResponse(ImmutableSet<SoP> matchingSops, IncidentType incidentType, StandardOfProof standardOfProof, ImmutableList<Function<SoPReferenceResponse, SoPReferenceResponse>> postProcessorsOrdered) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<SoPFactorsResponse> sopFactorsResponses = matchingSops.stream()
                .filter(s -> s.getStandardOfProof() == standardOfProof)
                .map(s -> DtoTransformations.fromSop(s,incidentType))
                .collect(Collectors.toList());

        SoPReferenceResponse dtoToReturn = new SoPReferenceResponse(sopFactorsResponses);

        if (postProcessorsOrdered != null && !postProcessorsOrdered.isEmpty())
        {
            for (Function<SoPReferenceResponse,SoPReferenceResponse> pp : postProcessorsOrdered)
            {
                dtoToReturn = pp.apply(dtoToReturn);
            }
        }

        String jsonString = null;
        try {
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dtoToReturn);
        } catch (JsonProcessingException e) {
            throw new DvaSopApiRuntimeException(e);
        }
        return jsonString;
    }

    public static ImmutableSet<SoP> getMatchingSopsByConditionName(String conditionName, ImmutableSet<SoP> toMatch, OffsetDateTime testTime)
    {

        ImmutableSet<SoP> matchingOnName = ImmutableSet.copyOf(
                toMatch.stream()
                        .filter(s -> isCurrent(s,testTime))
                        .filter(soP -> soP.getConditionName().equalsIgnoreCase(conditionName))
                        .collect(Collectors.toList()));

        return matchingOnName;
    }

    public static ImmutableSet<SoP> getMatchingSopsByIcdCode(ICDCode icdCode, ImmutableSet<SoP> toMatch, OffsetDateTime testTime)
    {
        ImmutableSet<SoP> matchingOnIcdCode = ImmutableSet.copyOf(
                toMatch.stream()
                        .filter(s -> isCurrent(s,testTime))
                        .filter(
                                soP -> soP.getICDCodes().stream().anyMatch(
                                        icdCodeInSop -> icdCodeInSop.equals(icdCode)))
                        .collect(Collectors.toSet()));

        return matchingOnIcdCode;
    }

    private static Boolean isCurrent(SoP testSop, OffsetDateTime testTime)
    {
        OffsetDateTime sopStartDateAsActOdt = DateTimeUtils.localDateToLastMidnightCanberraTime(testSop.getEffectiveFromDate());
        Optional<OffsetDateTime> endDateAsActOdt = testSop.getEndDate().isPresent() ?
                Optional.of(DateTimeUtils.localDateToNextMidnightCanberraTime(testSop.getEndDate().get()))
                : Optional.empty();

        Boolean isCurrent =  (sopStartDateAsActOdt.isBefore(testTime) || sopStartDateAsActOdt.isEqual(testTime))
                && (!endDateAsActOdt.isPresent() || endDateAsActOdt.get().isAfter(testTime));
        return isCurrent;
    }


    public static ImmutableSet<SoPPair> groupSopsToPairs(ImmutableSet<SoP> allSops,OffsetDateTime testDate)
    {
       Map<String,List<SoP>> groupedByName =  allSops.stream()
               .filter(s -> isCurrent(s,testDate))
               .collect(Collectors.groupingBy(sop -> sop.getConditionName()));

       Stream<Optional<SoPPair>> pairs = groupedByName.keySet()
               .stream().map(conditionName -> {

                   Optional<SoP> bopSop = groupedByName.get(conditionName).stream().filter(soP -> soP.getStandardOfProof() == StandardOfProof.BalanceOfProbabilities).findFirst();
                   Optional<SoP> rhSop = groupedByName.get(conditionName).stream().filter(soP -> soP.getStandardOfProof() == StandardOfProof.ReasonableHypothesis).findFirst();
                   if (bopSop.isPresent() && rhSop.isPresent()) {
                           return Optional.of(new SoPPair(bopSop.get(), rhSop.get()));
                   }
                   else {
                       logger.warn(String.format("No complete SoP pair for condition: %s", conditionName));
                       return Optional.empty();
                   }
               });

       List<SoPPair> wherePresent = pairs.filter(p -> p.isPresent()).map(s -> s.get()).collect(Collectors.toList());
       return ImmutableSet.copyOf(wherePresent);
    }




}
