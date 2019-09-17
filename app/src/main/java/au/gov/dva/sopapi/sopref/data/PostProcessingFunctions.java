package au.gov.dva.sopapi.sopref.data;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.DefinedTerm;
import au.gov.dva.sopapi.dtos.sopref.FactorDto;
import au.gov.dva.sopapi.dtos.sopref.SoPFactorsResponse;
import au.gov.dva.sopapi.dtos.sopref.SoPReferenceResponse;
import au.gov.dva.sopapi.exceptions.DvaSopApiRuntimeException;
import au.gov.dva.sopapi.interfaces.CuratedTextRepository;
import au.gov.dva.sopapi.interfaces.model.SoP;
import au.gov.dva.sopapi.sopref.DtoTransformations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import scala.util.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PostProcessingFunctions {

    public static Function<SoPReferenceResponse, SoPReferenceResponse> SubInCuratedDefinitions(CuratedTextRepository curatedTextRepository) {

        Function<SoPReferenceResponse,SoPReferenceResponse> toReturn = (before) -> {

            Stream<SoPFactorsResponse> soPFactorsResponseWithAppendedFactors =
                    before.getSoPFactorsResponses().stream()
                            .map(soPFactorsResponse -> {
                                List<FactorDto> factorWithAppendage = soPFactorsResponse.get_factors().stream().map(f -> appendCuratedDefinitions(f,curatedTextRepository)).collect(Collectors.toList());
                                return new SoPFactorsResponse(soPFactorsResponse.get_registerId(), soPFactorsResponse.get_citation(), soPFactorsResponse.get_instrumentNumber(),
                                        factorWithAppendage);
                            });

            return new SoPReferenceResponse(soPFactorsResponseWithAppendedFactors.collect(Collectors.toList()));
        };

        return toReturn;

    }


    public static Function<SoPReferenceResponse, SoPReferenceResponse> SubInCuratedFactorText(CuratedTextRepository curatedTextRepository)
    {
        Function<SoPReferenceResponse,SoPReferenceResponse> toReturn = (before) -> {

            Stream<SoPFactorsResponse> soPFactorsResponseWithAppendedFactors =
                    before.getSoPFactorsResponses().stream()
                            .map(soPFactorsResponse -> {
                                List<FactorDto> factorsWithTextReplaced = soPFactorsResponse.get_factors().stream()
                                        .map(f -> substituteCuratedFactorText(soPFactorsResponse.get_registerId(), f,curatedTextRepository)).collect(Collectors.toList());
                                return new SoPFactorsResponse(soPFactorsResponse.get_registerId(), soPFactorsResponse.get_citation(), soPFactorsResponse.get_instrumentNumber(),
                                        factorsWithTextReplaced);
                            });

            return new SoPReferenceResponse(soPFactorsResponseWithAppendedFactors.collect(Collectors.toList()));
        };

        return toReturn;
    }

    private static FactorDto  substituteCuratedFactorText(String registerId, FactorDto f, CuratedTextRepository curatedTextRepository) {
        if (curatedTextRepository.getFactorTextFor(registerId,f.get_paragraph()).isPresent())
        {
            String newText = curatedTextRepository.getFactorTextFor(registerId,f.get_paragraph()).get();
            return new FactorDto(f.get_paragraph(),newText,f.get_definedTerms());
        }
        else {
            return f;
        }
    }


    public static Function<SoPReferenceResponse,SoPReferenceResponse> AddStressorDefinitionsToText = (before) ->  {

        ImmutableList<String> stressorTerms = ImmutableList.of(
                "a category 1A stressor",
                "a category 1B stressor"
        );

        Stream<SoPFactorsResponse> soPFactorsResponseWithAppendedFactors =
                before.getSoPFactorsResponses().stream()
                        .map(soPFactorsResponse -> {
                            List<FactorDto> factorWithAppendage = soPFactorsResponse.get_factors().stream().map(f -> appendSoPDefinitions(f,stressorTerms)).collect(Collectors.toList());
                            return new SoPFactorsResponse(soPFactorsResponse.get_registerId(),soPFactorsResponse.get_citation(),soPFactorsResponse.get_instrumentNumber(),
                                    factorWithAppendage);
                        });

        return new SoPReferenceResponse(soPFactorsResponseWithAppendedFactors.collect(Collectors.toList()));

    };

    private static FactorDto appendSoPDefinitions(FactorDto factorDto, ImmutableList<String> definedTerms)
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

            String factorText = appendDefinitionToFactorText(factorDto.get_text(),definitions);
            FactorDto newDto = new FactorDto(factorDto.get_paragraph(),factorText,factorDto.get_definedTerms());
            return newDto;
        }
    }

    private static FactorDto appendCuratedDefinitions(FactorDto factorDto, CuratedTextRepository curatedTextRepository) {
        // check for defined terms
        List<DefinedTerm> definedTerms = factorDto.get_definedTerms();
        List<String> termsDefinedInCuratedRepository = definedTerms.stream()
                .filter(dt -> curatedTextRepository.getDefinitionFor(dt.get_term()).isPresent())
                .map(dt -> String.format("<b>'%s'</b> %s", dt.get_term(), curatedTextRepository.getDefinitionFor(dt.get_term()).get()))
                .collect(Collectors.toList());

        if (!termsDefinedInCuratedRepository.isEmpty())
        {
            String newFactorText = appendDefinitionToFactorText(factorDto.get_text(),termsDefinedInCuratedRepository);
            return new FactorDto(factorDto.get_paragraph(),newFactorText,factorDto.get_definedTerms());
        }
        else {
            return factorDto;
        }
    }

    private static String appendDefinitionToFactorText(String factorText, List<String> definitions) {
        String toAppend = String.format("(%s)", String.join(Properties.lineSeparator(),definitions));
        String factorTextWithDefinitionsAppended = String.format("%s%n%s",factorText,toAppend);
        return factorTextWithDefinitionsAppended;
    }





}
