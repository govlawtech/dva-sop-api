package au.gov.dva.sopapi;

import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.components.OperationalServiceDto;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SemanticRequestValidation {

    public static ImmutableList<String> getSemanticErrors(SopSupportRequestDto sopSupportRequestDto)
    {
        List<String> errors = new ArrayList<>();

        List<String> opsWithInvalidDates = sopSupportRequestDto.get_serviceHistoryDto().get_serviceDetailsArray().stream()
                .flatMap(s -> s.get_operationalServiceDtos().stream())
                .filter(o -> o.get_startDate() == null || o.get_endDate() != null && o.get_startDate().isAfter(o.get_endDate()))
                .map(o -> String.format("%s has no start date or start date after end date.", o.get_description()))
                .collect(Collectors.toList());
        errors.addAll(opsWithInvalidDates);


        boolean serviceWithInvalidDatesExist = sopSupportRequestDto.get_serviceHistoryDto().get_serviceDetailsArray().stream()
                .anyMatch(o ->  o.get_startDate() == null || o.get_endDate() != null && o.get_startDate().isAfter(o.get_endDate()));
        if (serviceWithInvalidDatesExist) errors.add("There is at least one service with no start date or start date after end date.");

        return ImmutableList.copyOf(errors);
    }
}
