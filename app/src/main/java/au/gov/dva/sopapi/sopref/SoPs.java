package au.gov.dva.sopapi.sopref;

import au.gov.dva.sopapi.DateTimeUtils;
import au.gov.dva.sopapi.Environment;
import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.DefinedTerm;
import au.gov.dva.sopapi.dtos.sopref.FactorDto;
import au.gov.dva.sopapi.dtos.sopref.SoPFactorsResponse;
import au.gov.dva.sopapi.dtos.sopref.SoPReferenceResponse;
import au.gov.dva.sopapi.exceptions.DvaSopApiRuntimeException;
import au.gov.dva.sopapi.interfaces.model.Factor;
import au.gov.dva.sopapi.interfaces.model.ICDCode;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.interfaces.model.SoPPair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.netty.util.internal.chmv8.ConcurrentHashMapV8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.util.Properties;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SoPs {

    static Logger logger = LoggerFactory.getLogger(SoPs.class);

    public static Function<SoPReferenceResponse,SoPReferenceResponse> AddStressorDefinitionsToText = (before) ->  {

        ImmutableList<String> stressorTerms = ImmutableList.of(
                "a category 1A stressor",
                "a category 1B stressor"
        );

        Stream<SoPFactorsResponse> soPFactorsResponseWithAppendedFactors =
                before.getSoPFactorsResponses().stream()
                .map(soPFactorsResponse -> {
                    List<FactorDto> factorWithAppendage = soPFactorsResponse.get_factors().stream().map(f -> AppendDefinitions(f,stressorTerms)).collect(Collectors.toList());
                    return new SoPFactorsResponse(soPFactorsResponse.get_registerId(),soPFactorsResponse.get_citation(),soPFactorsResponse.get_instrumentNumber(),
                            factorWithAppendage);
                });

        return new SoPReferenceResponse(soPFactorsResponseWithAppendedFactors.collect(Collectors.toList()));

    };

    private static FactorDto AppendDefinitions(FactorDto factorDto, ImmutableList<String> definedTerms)
    {

        List<String> definitions = new ArrayList<>();
        for (String definedTerm: definedTerms) {
            Optional<DefinedTerm> matchingDefinition = factorDto.get_definedTerms().stream().filter(dt -> dt.get_term().toLowerCase().contentEquals(definedTerm.toLowerCase())).findFirst();
            if (matchingDefinition.isPresent())
            {
                 definitions.add(String.format("<b>'%s'</b> %s",definedTerm,matchingDefinition.get().get_definition()));
            }
        }

        if (definitions.isEmpty())
        {
            return factorDto;
        }
        else {
            String toAppend = String.format("(%s)", String.join(Properties.lineSeparator(),definitions));
            String factorText = String.format("%s%n%s",factorDto.get_text(),toAppend);
            FactorDto newDto = new FactorDto(factorDto.get_paragraph(),factorText,factorDto.get_definedTerms());
            return newDto;
        }
    }




    public static String buildSopRefJsonResponse(ImmutableSet<SoP> matchingSops, IncidentType incidentType, StandardOfProof standardOfProof, Function<SoPReferenceResponse, SoPReferenceResponse> postProcessor) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<SoPFactorsResponse> sopFactorsResponses = matchingSops.stream()
                .filter(s -> s.getStandardOfProof() == standardOfProof)
                .map(s -> DtoTransformations.fromSop(s,incidentType))
                .collect(Collectors.toList());

        SoPReferenceResponse dtoToReturn = new SoPReferenceResponse(sopFactorsResponses);

        if (postProcessor != null)
        {
            dtoToReturn = postProcessor.apply(dtoToReturn);
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
