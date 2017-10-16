package au.gov.dva.sopapi.client;

import au.gov.dva.sopapi.dtos.IncidentType;
import au.gov.dva.sopapi.dtos.StandardOfProof;
import au.gov.dva.sopapi.dtos.sopref.OperationsResponse;
import au.gov.dva.sopapi.dtos.sopref.SoPReferenceResponse;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportRequestDto;
import au.gov.dva.sopapi.dtos.sopsupport.SopSupportResponseDto;

import java.util.concurrent.CompletableFuture;

/**
 * Created by mc on 8/05/17.
 */
public interface SoPApiClient {
    CompletableFuture<SoPReferenceResponse> getFactorsForConditionName(String conditionName, IncidentType incidentType, StandardOfProof standardOfProof);

    CompletableFuture<SoPReferenceResponse> getFactorsForIcdCode(String icdCodeVersion, String icdCodeValue, IncidentType incidentType, StandardOfProof standardOfProof);

    CompletableFuture<OperationsResponse> getOperations();

    CompletableFuture<SopSupportResponseDto> getSatisfiedFactors(SopSupportRequestDto sopSupportRequestDto);

}
