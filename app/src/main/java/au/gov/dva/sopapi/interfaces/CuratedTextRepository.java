package au.gov.dva.sopapi.interfaces;

import java.util.Optional;

public interface CuratedTextRepository {

    Optional<String> getDefinitionFor(String definedTerm);
    Optional<String> getFactorTextFor(String registerId, String legalReference);
}
