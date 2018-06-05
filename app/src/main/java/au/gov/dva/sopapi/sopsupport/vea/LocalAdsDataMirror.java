package au.gov.dva.sopapi.sopsupport.vea;

import au.gov.dva.sopapi.interfaces.ActDeterminationServiceClient;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class LocalAdsDataMirror implements ActDeterminationServiceClient {

    private ImmutableList<ServiceRegion> serviceRegions;

    public LocalAdsDataMirror(ImmutableList<ServiceRegion> serviceRegions)
    {
        this.serviceRegions = serviceRegions;
    }

    @Override
    public CompletableFuture<Boolean> matchesWhiteFilter(String operationName, Predicate<List<ServiceRegion>> whiteFilter) {
        CompletableFuture<Boolean> promise = CompletableFuture.completedFuture(whiteFilter.test(getRegionsMatchingName(operationName)));
        return promise;
    }

    @Override
    public CompletableFuture<Boolean> isOperational(String operationName) {
        return CompletableFuture.completedFuture(
                getRegionsMatchingName(operationName).stream()
                .anyMatch(sr ->
                        (sr.isOperational() ||
                                sr.isWarlike() ||
                                sr.isHazardous() ||
                                sr.isPeacekeeping()) &&
                                !sr.isMrcaNonWarlike() &&
                                !sr.isMrcaWarlike()));
    }


    private ImmutableList<ServiceRegion> getRegionsMatchingName(String operationName)
    {
        // same logic as https://github.com/AusDVA/dva-act-determination-service/blob/81b01cc7c7dca751411b5b8fd710c9f8785cc161/src/main/java/au/gov/dva/predicate/FindOperationPredicate.java#L21
        return serviceRegions.stream()
                .filter(serviceRegion -> StringUtils.containsIgnoreCase(operationName,"(" + serviceRegion.getOperationName() + ")"))
                .collect(Collectors.collectingAndThen(Collectors.toList(),ImmutableList::copyOf));
    }


}
